package com.securities.securities_server.domain.order.service;

import com.securities.securities_server.domain.cashwallet.entity.CashWallet;
import com.securities.securities_server.domain.cashwallet.repository.CashWalletRepository;
import com.securities.securities_server.domain.market.entity.MarketStatus;
import com.securities.securities_server.domain.market.repository.MarketStatusRepository;
import com.securities.securities_server.domain.market.service.TickSizeCalculator;
import com.securities.securities_server.domain.order.controller.request.PlaceOrderRequest;
import com.securities.securities_server.domain.order.entity.Order;
import com.securities.securities_server.domain.order.repository.OrderRepository;
import com.securities.securities_server.domain.stock.entity.Stock;
import com.securities.securities_server.domain.stock.repository.StockRepository;
import com.securities.securities_server.domain.stockwallet.entity.StockWallet;
import com.securities.securities_server.domain.stockwallet.repository.StockWalletRepository;
import com.securities.securities_server.domain.user.entity.User;
import com.securities.securities_server.domain.user.repository.UserRepository;
import com.securities.securities_server.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.securities.securities_server.domain.order.entity.OrderSide.BUY;
import static com.securities.securities_server.domain.order.entity.OrderSide.SELL;
import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.INVALID_ORDER_PRICE;
import static com.securities.securities_server.global.exception.ErrorCode.MARKET_STATUS_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.STOCK_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.STOCK_WALLET_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderCreateService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;
    private final CashWalletRepository cashWalletRepository;
    private final StockWalletRepository stockWalletRepository;
    private final MarketStatusRepository marketStatusRepository;

    private final TickSizeCalculator tickSizeCalculator;

    @Transactional
    public Order createOrder(Long userId, PlaceOrderRequest request) {
        User user = getUser(userId);
        Stock stock = getStock(request.stockId());

        CashWallet cashWallet = getCashWallet(userId);
        cashWallet.validateNotBlocked();

        StockWallet stockWallet = null;
        if (request.side() == SELL) {
            stockWallet = getStockWallet(userId, request.stockId());
            stockWallet.validateNotBlocked();
        }

        LocalDate tradingDate = LocalDate.now();
        MarketStatus marketStatus = getMarketStatus(request.stockId(), tradingDate);
        validateOrderPrice(request.price(), marketStatus.getLowerLimitPrice(), marketStatus.getUpperLimitPrice());

        Order order = Order.create(user, stock, request.side(), request.price(), request.quantity());
        lockResource(order, cashWallet, stockWallet);

        return orderRepository.save(order);
    }

    private void lockResource(Order order, CashWallet cashWallet, StockWallet stockWallet) {
        if (order.getSide() == BUY) {
            cashWallet.lock(order.getPrice() * order.getQuantity());
        } else if (order.getSide() == SELL) {
            stockWallet.lock(order.getQuantity());
        }
    }

    private Stock getStock(Long stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new CustomException(STOCK_NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private CashWallet getCashWallet(Long userId) {
        return cashWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(CASH_WALLET_NOT_FOUND));
    }

    private StockWallet getStockWallet(Long userId, Long stockId) {
        return stockWalletRepository.findByUserIdAndStockId(userId, stockId)
                .orElseThrow(() -> new CustomException(STOCK_WALLET_NOT_FOUND));
    }

    private MarketStatus getMarketStatus(Long stockId, LocalDate tradingDate) {
        return marketStatusRepository.findByStockIdAndTradingDate(stockId, tradingDate)
                .orElseThrow(() -> new CustomException(MARKET_STATUS_NOT_FOUND));
    }

    private void validateOrderPrice(long price, long lowerLimitPrice, long upperLimitPrice) {
        if (!tickSizeCalculator.validatePrice(price)) {
            throw new CustomException(INVALID_ORDER_PRICE);
        }
        if (price < lowerLimitPrice || price > upperLimitPrice) {
            throw new CustomException(INVALID_ORDER_PRICE);
        }
    }
}
