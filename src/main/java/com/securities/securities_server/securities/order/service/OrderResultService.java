package com.securities.securities_server.securities.order.service;

import com.securities.securities_server.exchange.order.dto.response.ExchangeOrderResponse;
import com.securities.securities_server.exchange.order.dto.response.MatchedMakerOrder;
import com.securities.securities_server.securities.cashwallet.entity.CashWallet;
import com.securities.securities_server.securities.cashwallet.entity.CashWalletTxType;
import com.securities.securities_server.securities.cashwallet.repository.CashWalletRepository;
import com.securities.securities_server.securities.cashwallet.service.CashWalletHistoryService;
import com.securities.securities_server.securities.cashwallet.service.dto.CashWalletHistoryCommand;
import com.securities.securities_server.securities.market.entity.MarketStatus;
import com.securities.securities_server.securities.market.repository.MarketStatusRepository;
import com.securities.securities_server.securities.match.entity.Match;
import com.securities.securities_server.securities.match.repository.MatchRepository;
import com.securities.securities_server.securities.order.entity.Order;
import com.securities.securities_server.securities.order.repository.OrderRepository;
import com.securities.securities_server.securities.stock.entity.Stock;
import com.securities.securities_server.securities.stockwallet.entity.StockWallet;
import com.securities.securities_server.securities.stockwallet.entity.StockWalletTxType;
import com.securities.securities_server.securities.stockwallet.repository.StockWalletRepository;
import com.securities.securities_server.securities.stockwallet.service.StockWalletHistoryService;
import com.securities.securities_server.securities.stockwallet.service.dto.StockWalletHistoryCommand;
import com.securities.securities_server.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.securities.securities_server.securities.cashwallet.entity.CashWalletTxType.TRADE_PAY;
import static com.securities.securities_server.securities.cashwallet.entity.CashWalletTxType.TRADE_RECEIVE;
import static com.securities.securities_server.securities.cashwallet.entity.CashWalletTxType.TRADE_REFUND;
import static com.securities.securities_server.securities.order.entity.MatchResult.CANCELLED;
import static com.securities.securities_server.securities.order.entity.MatchResult.MATCHED;
import static com.securities.securities_server.securities.order.entity.MatchResult.UNMATCHED;
import static com.securities.securities_server.securities.order.entity.OrderSide.BUY;
import static com.securities.securities_server.securities.order.entity.OrderSide.SELL;
import static com.securities.securities_server.securities.stockwallet.entity.StockWalletTxType.BUY_EXECUTED;
import static com.securities.securities_server.securities.stockwallet.entity.StockWalletTxType.SELL_EXECUTED;
import static com.securities.securities_server.securities.stockwallet.entity.StockWalletTxType.SELL_ORDER_CANCELED;
import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.MARKET_STATUS_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.ORDER_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.STOCK_WALLET_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderResultService {

    private final OrderRepository orderRepository;
    private final CashWalletRepository cashWalletRepository;
    private final StockWalletRepository stockWalletRepository;
    private final MatchRepository matchRepository;
    private final MarketStatusRepository marketStatusRepository;

    private final CashWalletHistoryService cashWalletHistoryService;
    private final StockWalletHistoryService stockWalletHistoryService;

    @Transactional
    public void handleExchangeOrderResponse(ExchangeOrderResponse response, Long orderId) {
        if (response.matchResult() == UNMATCHED) {
        } else if (response.matchResult() == MATCHED) {
            handleMatched(response, orderId);
        } else if (response.matchResult() == CANCELLED) {
            handleCancelled(orderId);
        }
    }

    private void handleMatched(ExchangeOrderResponse response, Long orderId) {
        Order takerOrder = getOrder(orderId);

        for (MatchedMakerOrder maker : response.makers()) {
            Order makerOrder = getOrder(maker.orderId());

            Order buyerOrder;
            Order sellerOrder;

            if (takerOrder.getSide() == SELL) {
                sellerOrder = takerOrder;
                buyerOrder = makerOrder;
            } else {
                buyerOrder = takerOrder;
                sellerOrder = makerOrder;
            }
            long matchedQuantity = maker.matchedQuantity();
            long txAmount = matchedQuantity * response.price();

            processMatchedTrade(buyerOrder, sellerOrder, txAmount, matchedQuantity);
            saveMatch(makerOrder, takerOrder);
            updateMarketStatus(takerOrder.getStock(), response.price(), matchedQuantity);
        }
    }

    private void handleCancelled(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getSide() == BUY) {
            cancelBuyOrder(order);
        } else {
            cancelSellOrder(order);
        }
    }

    private void cancelSellOrder(Order order) {
        StockWallet stockWallet = getSellerStockWallet(order);
        long cancelQuantity = order.getUnfilledQuantity();

        order.cancelRemainingQuantity();
        stockWallet.unlock(cancelQuantity);
        StockWalletHistoryCommand command =
                new StockWalletHistoryCommand(
                        stockWallet,
                        SELL_ORDER_CANCELED,
                        cancelQuantity,
                        stockWallet.getHoldingQuantity()
                );
        stockWalletHistoryService.createStockWalletHistory(command);
    }

    private void cancelBuyOrder(Order order) {
        CashWallet cashWallet = getCashWallet(order);
        long cancelAmount = order.getPrice() * order.getUnfilledQuantity();

        order.cancelRemainingQuantity();
        cashWallet.unlock(cancelAmount);
        CashWalletHistoryCommand command =
                new CashWalletHistoryCommand(cashWallet, TRADE_REFUND, cancelAmount, cashWallet.getBalance());
        cashWalletHistoryService.createCashWalletHistory(command);
    }

    private void saveMatch(Order makerOrder, Order takerOrder) {
        Match match = Match.create(makerOrder, takerOrder);
        matchRepository.save(match);
    }

    private void updateMarketStatus(Stock stock, long price, long quantity) {
        LocalDate now = LocalDate.now();
        MarketStatus marketStatus = marketStatusRepository.findByStockIdAndTradingDate(stock.getId(), now)
                .orElseThrow(() -> new CustomException(MARKET_STATUS_NOT_FOUND));

        marketStatus.applyTrade(price, quantity);
    }

    private void processMatchedTrade(
            Order buyerOrder,
            Order sellerOrder,
            long txAmount,
            long matchedQuantity
    ) {
        StockWallet buyerStockWallet = getBuyerStockWallet(buyerOrder);
        CashWallet buyerCashWallet = getCashWallet(buyerOrder);

        StockWallet sellerStockWallet = getSellerStockWallet(sellerOrder);
        CashWallet sellerCashWallet = getCashWallet(sellerOrder);

        buyerOrder.fill(matchedQuantity);
        buyerProcess(buyerStockWallet, buyerCashWallet, txAmount, matchedQuantity);

        sellerOrder.fill(matchedQuantity);
        sellerProcess(sellerStockWallet, sellerCashWallet, txAmount, matchedQuantity);
    }

    private void sellerProcess(StockWallet stockWallet, CashWallet cashWallet, long txAmount, long matchedQuantity) {
        stockWallet.sellLockedQuantity(matchedQuantity);
        saveStockWalletHistory(stockWallet, SELL_EXECUTED, matchedQuantity);

        cashWallet.deposit(txAmount);
        saveCashWalletHistory(cashWallet, TRADE_RECEIVE, txAmount);
    }

    private void buyerProcess(StockWallet stockWallet, CashWallet cashWallet, long txAmount, long matchedQuantity) {
        cashWallet.payLockedAmount(txAmount);
        saveCashWalletHistory(cashWallet, TRADE_PAY, txAmount);

        stockWallet.credit(matchedQuantity);
        saveStockWalletHistory(stockWallet, BUY_EXECUTED, matchedQuantity);
    }

    private void saveCashWalletHistory(CashWallet cashWallet, CashWalletTxType txType, long txAmount) {
        CashWalletHistoryCommand command =
                new CashWalletHistoryCommand(
                        cashWallet,
                        txType,
                        txAmount,
                        cashWallet.getBalance()
                );
        cashWalletHistoryService.createCashWalletHistory(command);
    }

    private void saveStockWalletHistory(StockWallet stockWallet, StockWalletTxType txType, long txQuantity) {
        StockWalletHistoryCommand command = new StockWalletHistoryCommand(
                stockWallet,
                txType,
                txQuantity,
                stockWallet.getHoldingQuantity()
        );
        stockWalletHistoryService.createStockWalletHistory(command);
    }

    private StockWallet getSellerStockWallet(Order order) {
        return stockWalletRepository.findByUserIdAndStockId(order.getUser().getId(), order.getStock().getId())
                .orElseThrow(() -> new CustomException(STOCK_WALLET_NOT_FOUND));
    }

    private StockWallet getBuyerStockWallet(Order order) {
        return stockWalletRepository.findByUserIdAndStockId(order.getUser().getId(), order.getStock().getId())
                .orElseGet(() -> stockWalletRepository.save(
                        StockWallet.create(order.getUser(), order.getStock())
                ));
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));
    }

    private CashWallet getCashWallet(Order order) {
        return cashWalletRepository.findByUserId(order.getUser().getId())
                .orElseThrow(() -> new CustomException(CASH_WALLET_NOT_FOUND));
    }
}
