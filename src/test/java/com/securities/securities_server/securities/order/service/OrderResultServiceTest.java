package com.securities.securities_server.securities.order.service;

import com.securities.securities_server.exchange.order.dto.response.ExchangeOrderResponse;
import com.securities.securities_server.exchange.order.dto.response.MatchedMakerOrder;
import com.securities.securities_server.securities.cashwallet.entity.CashWallet;
import com.securities.securities_server.securities.cashwallet.repository.CashWalletRepository;
import com.securities.securities_server.securities.cashwallet.service.CashWalletHistoryService;
import com.securities.securities_server.securities.market.entity.MarketStatus;
import com.securities.securities_server.securities.market.repository.MarketStatusRepository;
import com.securities.securities_server.securities.market.service.dto.MarketPriceInfo;
import com.securities.securities_server.securities.match.entity.Match;
import com.securities.securities_server.securities.match.repository.MatchRepository;
import com.securities.securities_server.global.common.MatchResult;
import com.securities.securities_server.securities.order.entity.Order;
import com.securities.securities_server.securities.order.repository.OrderRepository;
import com.securities.securities_server.securities.stock.entity.Stock;
import com.securities.securities_server.securities.stockwallet.entity.StockWallet;
import com.securities.securities_server.securities.stockwallet.repository.StockWalletRepository;
import com.securities.securities_server.securities.stockwallet.service.StockWalletHistoryService;
import com.securities.securities_server.securities.user.entity.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.securities.securities_server.global.common.OrderSide.BUY;
import static com.securities.securities_server.global.common.OrderSide.SELL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OrderResultServiceTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    CashWalletRepository cashWalletRepository;
    @Mock
    StockWalletRepository stockWalletRepository;
    @Mock
    MatchRepository matchRepository;
    @Mock
    MarketStatusRepository marketStatusRepository;
    @Mock
    CashWalletHistoryService cashWalletHistoryService;
    @Mock
    StockWalletHistoryService stockWalletHistoryService;

    @InjectMocks
    private OrderResultService orderResultService;

    @Test
    void 거래소_응답이_MATCHED이고_taker가_매수자이면_체결_결과를_반영한다() {
        // given
        User buyer = createUser(1L, "buyer");
        User seller = createUser(2L, "seller");
        Stock stock = createStock(1L);

        Order takerBuyOrder = createBuyOrder(1L, buyer, stock, 10000L, 10L);
        Order makerSellOrder = createSellOrder(2L, seller, stock, 10000L, 10L);

        CashWallet buyerCashWallet = createBuyerCashWallet(buyer, 100000L);
        CashWallet sellerCashWallet = createSellerCashWallet(seller);
        StockWallet buyerStockWallet = createBuyerStockWallet(buyer, stock);
        StockWallet sellerStockWallet = createSellerStockWallet(seller, stock, 10L);
        MarketStatus marketStatus = createMarketStatus(stock);

        ExchangeOrderResponse response =
                createMatchedResponse(
                        takerBuyOrder.getId(),
                        makerSellOrder.getId(),
                        10000L,
                        10L
                );

        given(orderRepository.findById(1L)).willReturn(Optional.of(takerBuyOrder));
        given(orderRepository.findById(2L)).willReturn(Optional.of(makerSellOrder));
        given(cashWalletRepository.findByUserId(1L)).willReturn(Optional.of(buyerCashWallet));
        given(cashWalletRepository.findByUserId(2L)).willReturn(Optional.of(sellerCashWallet));
        given(stockWalletRepository.findByUserIdAndStockId(1L, 1L))
                .willReturn(Optional.of(buyerStockWallet));
        given(stockWalletRepository.findByUserIdAndStockId(2L, 1L))
                .willReturn(Optional.of(sellerStockWallet));
        given(marketStatusRepository.findByStockIdAndTradingDate(
                eq(1L),
                any(LocalDate.class)
        )).willReturn(Optional.of(marketStatus));

        // when
        orderResultService.handleExchangeOrderResponse(response, takerBuyOrder.getId());

        // then
        assertThat(takerBuyOrder.getUnfilledQuantity()).isZero();
        assertThat(makerSellOrder.getUnfilledQuantity()).isZero();

        assertThat(buyerCashWallet.getBalance()).isZero();
        assertThat(buyerCashWallet.getLockedAmount()).isZero();
        assertThat(buyerStockWallet.getHoldingQuantity()).isEqualTo(10L);

        assertThat(sellerCashWallet.getBalance()).isEqualTo(100000L);
        assertThat(sellerStockWallet.getHoldingQuantity()).isZero();
        assertThat(sellerStockWallet.getLockedQuantity()).isZero();

        assertThat(marketStatus.getTradingVolume()).isEqualTo(10L);
        assertThat(marketStatus.getTradingAmount()).isEqualTo(100000L);

        verify(matchRepository).save(any(Match.class));
        verify(cashWalletHistoryService, times(2)).createCashWalletHistory(any());
        verify(stockWalletHistoryService, times(2)).createStockWalletHistory(any());
    }

    @Test
    void 거래소_응답이_MATCHED이고_taker가_매도자이면_체결_결과를_반영한다() {
        // given
        User buyer = createUser(1L, "buyer");
        User seller = createUser(2L, "seller");
        Stock stock = createStock(1L);

        Order takerSellOrder = createSellOrder(1L, seller, stock, 10000L, 10L);
        Order makerBuyOrder = createBuyOrder(2L, buyer, stock, 10000L, 10L);

        CashWallet buyerCashWallet = createBuyerCashWallet(buyer, 100000L);
        CashWallet sellerCashWallet = createSellerCashWallet(seller);
        StockWallet buyerStockWallet = createBuyerStockWallet(buyer, stock);
        StockWallet sellerStockWallet = createSellerStockWallet(seller, stock, 10L);
        MarketStatus marketStatus = createMarketStatus(stock);

        ExchangeOrderResponse response =
                createMatchedResponse(
                        takerSellOrder.getId(),
                        makerBuyOrder.getId(),
                        10000L,
                        10L
                );

        given(orderRepository.findById(1L)).willReturn(Optional.of(takerSellOrder));
        given(orderRepository.findById(2L)).willReturn(Optional.of(makerBuyOrder));
        given(cashWalletRepository.findByUserId(1L)).willReturn(Optional.of(buyerCashWallet));
        given(cashWalletRepository.findByUserId(2L)).willReturn(Optional.of(sellerCashWallet));
        given(stockWalletRepository.findByUserIdAndStockId(1L, 1L))
                .willReturn(Optional.of(buyerStockWallet));
        given(stockWalletRepository.findByUserIdAndStockId(2L, 1L))
                .willReturn(Optional.of(sellerStockWallet));
        given(marketStatusRepository.findByStockIdAndTradingDate(
                eq(1L),
                any(LocalDate.class)
        )).willReturn(Optional.of(marketStatus));

        // when
        orderResultService.handleExchangeOrderResponse(response, takerSellOrder.getId());

        // then
        assertThat(takerSellOrder.getUnfilledQuantity()).isZero();
        assertThat(makerBuyOrder.getUnfilledQuantity()).isZero();

        assertThat(buyerCashWallet.getBalance()).isZero();
        assertThat(buyerCashWallet.getLockedAmount()).isZero();
        assertThat(buyerStockWallet.getHoldingQuantity()).isEqualTo(10L);

        assertThat(sellerCashWallet.getBalance()).isEqualTo(100000L);
        assertThat(sellerStockWallet.getHoldingQuantity()).isZero();
        assertThat(sellerStockWallet.getLockedQuantity()).isZero();

        assertThat(marketStatus.getTradingVolume()).isEqualTo(10L);
        assertThat(marketStatus.getTradingAmount()).isEqualTo(100000L);

        verify(matchRepository).save(any(Match.class));
        verify(cashWalletHistoryService, times(2)).createCashWalletHistory(any());
        verify(stockWalletHistoryService, times(2)).createStockWalletHistory(any());
    }

    @Test
    void 여러_maker와_체결되면_각_체결_수량만큼_주문과_지갑을_반영한다() {
        // given
        User buyer = createUser(1L, "buyer");
        User seller1 = createUser(2L, "seller1");
        User seller2 = createUser(3L, "seller2");
        Stock stock = createStock(1L);

        Order takerBuyOrder = createBuyOrder(1L, buyer, stock, 10000L, 10L);
        Order makerSellOrder1 = createSellOrder(2L, seller1, stock, 10000L, 4L);
        Order makerSellOrder2 = createSellOrder(3L, seller2, stock, 10000L, 6L);

        CashWallet buyerCashWallet = createBuyerCashWallet(buyer, 100000L);
        CashWallet sellerCashWallet1 = CashWallet.create(seller1, "777876543211");
        CashWallet sellerCashWallet2 = CashWallet.create(seller2, "777876543212");

        StockWallet buyerStockWallet = createBuyerStockWallet(buyer, stock);
        StockWallet sellerStockWallet1 = createSellerStockWallet(seller1, stock, 4L);
        StockWallet sellerStockWallet2 = createSellerStockWallet(seller2, stock, 6L);

        MarketStatus marketStatus = createMarketStatus(stock);

        ExchangeOrderResponse response = new ExchangeOrderResponse(
                MatchResult.MATCHED,
                takerBuyOrder.getId(),
                List.of(
                        new MatchedMakerOrder(makerSellOrder1.getId(), 4L),
                        new MatchedMakerOrder(makerSellOrder2.getId(), 6L)
                ),
                10000L,
                10L
        );

        given(orderRepository.findById(1L)).willReturn(Optional.of(takerBuyOrder));
        given(orderRepository.findById(2L)).willReturn(Optional.of(makerSellOrder1));
        given(orderRepository.findById(3L)).willReturn(Optional.of(makerSellOrder2));

        given(cashWalletRepository.findByUserId(1L)).willReturn(Optional.of(buyerCashWallet));
        given(cashWalletRepository.findByUserId(2L)).willReturn(Optional.of(sellerCashWallet1));
        given(cashWalletRepository.findByUserId(3L)).willReturn(Optional.of(sellerCashWallet2));

        given(stockWalletRepository.findByUserIdAndStockId(1L, 1L))
                .willReturn(Optional.of(buyerStockWallet));
        given(stockWalletRepository.findByUserIdAndStockId(2L, 1L))
                .willReturn(Optional.of(sellerStockWallet1));
        given(stockWalletRepository.findByUserIdAndStockId(3L, 1L))
                .willReturn(Optional.of(sellerStockWallet2));

        given(marketStatusRepository.findByStockIdAndTradingDate(
                eq(1L),
                any(LocalDate.class)
        )).willReturn(Optional.of(marketStatus));

        // when
        orderResultService.handleExchangeOrderResponse(response, takerBuyOrder.getId());

        // then
        assertThat(takerBuyOrder.getUnfilledQuantity()).isZero();
        assertThat(makerSellOrder1.getUnfilledQuantity()).isZero();
        assertThat(makerSellOrder2.getUnfilledQuantity()).isZero();

        assertThat(buyerCashWallet.getBalance()).isZero();
        assertThat(buyerCashWallet.getLockedAmount()).isZero();
        assertThat(buyerStockWallet.getHoldingQuantity()).isEqualTo(10L);

        assertThat(sellerCashWallet1.getBalance()).isEqualTo(40000L);
        assertThat(sellerCashWallet2.getBalance()).isEqualTo(60000L);

        assertThat(sellerStockWallet1.getHoldingQuantity()).isZero();
        assertThat(sellerStockWallet1.getLockedQuantity()).isZero();

        assertThat(sellerStockWallet2.getHoldingQuantity()).isZero();
        assertThat(sellerStockWallet2.getLockedQuantity()).isZero();

        assertThat(marketStatus.getTradingVolume()).isEqualTo(10L);
        assertThat(marketStatus.getTradingAmount()).isEqualTo(100000L);

        verify(matchRepository, times(2)).save(any(Match.class));
        verify(cashWalletHistoryService, times(4)).createCashWalletHistory(any());
        verify(stockWalletHistoryService, times(4)).createStockWalletHistory(any());
    }

    @Test
    void 매수자_종목_계좌가_없으면_새로_생성한_뒤_매수_수량을_반영한다() {
        // given
        User buyer = createUser(1L, "buyer");
        User seller = createUser(2L, "seller");
        Stock stock = createStock(1L);

        Order takerBuyOrder = createBuyOrder(1L, buyer, stock, 10000L, 10L);
        Order makerSellOrder = createSellOrder(2L, seller, stock, 10000L, 10L);

        CashWallet buyerCashWallet = createBuyerCashWallet(buyer, 100000L);
        CashWallet sellerCashWallet = createSellerCashWallet(seller);

        StockWallet sellerStockWallet = createSellerStockWallet(seller, stock, 10L);
        MarketStatus marketStatus = createMarketStatus(stock);

        ExchangeOrderResponse response =
                createMatchedResponse(
                        takerBuyOrder.getId(),
                        makerSellOrder.getId(),
                        10000L,
                        10L
                );

        given(orderRepository.findById(1L)).willReturn(Optional.of(takerBuyOrder));
        given(orderRepository.findById(2L)).willReturn(Optional.of(makerSellOrder));

        given(cashWalletRepository.findByUserId(1L)).willReturn(Optional.of(buyerCashWallet));
        given(cashWalletRepository.findByUserId(2L)).willReturn(Optional.of(sellerCashWallet));

        given(stockWalletRepository.findByUserIdAndStockId(1L, 1L))
                .willReturn(Optional.empty());

        given(stockWalletRepository.save(any(StockWallet.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        given(stockWalletRepository.findByUserIdAndStockId(2L, 1L))
                .willReturn(Optional.of(sellerStockWallet));

        given(marketStatusRepository.findByStockIdAndTradingDate(
                eq(1L),
                any(LocalDate.class)
        )).willReturn(Optional.of(marketStatus));

        // when
        orderResultService.handleExchangeOrderResponse(response, takerBuyOrder.getId());

        // then
        verify(stockWalletRepository).save(any(StockWallet.class));

        verify(stockWalletHistoryService, times(2))
                .createStockWalletHistory(any());

        verify(cashWalletHistoryService, times(2))
                .createCashWalletHistory(any());

        verify(matchRepository).save(any(Match.class));

        assertThat(takerBuyOrder.getUnfilledQuantity()).isZero();
        assertThat(makerSellOrder.getUnfilledQuantity()).isZero();

        assertThat(buyerCashWallet.getBalance()).isZero();
        assertThat(buyerCashWallet.getLockedAmount()).isZero();

        assertThat(sellerCashWallet.getBalance()).isEqualTo(100000L);
        assertThat(sellerStockWallet.getHoldingQuantity()).isZero();
        assertThat(sellerStockWallet.getLockedQuantity()).isZero();

        assertThat(marketStatus.getTradingVolume()).isEqualTo(10L);
        assertThat(marketStatus.getTradingAmount()).isEqualTo(100000L);
    }

    @Test
    void 거래소_응답이_UNMATCHED이면_추가_처리를_하지_않는다() {
        // given
        ExchangeOrderResponse response = new ExchangeOrderResponse(
                MatchResult.UNMATCHED,
                null,
                List.of(),
                0L,
                0L
        );

        // when
        orderResultService.handleExchangeOrderResponse(response, 1L);

        // then
        verifyNoInteractions(
                orderRepository,
                cashWalletRepository,
                stockWalletRepository,
                matchRepository,
                marketStatusRepository,
                cashWalletHistoryService,
                stockWalletHistoryService
        );
    }

    @Test
    void 거래소_응답이_CANCELLED이고_매수_주문이면_미체결_금액을_해제한다() {
        // given
        User buyer = createUser(1L, "buyer");
        Stock stock = createStock(1L);

        Order buyOrder = createBuyOrder(1L, buyer, stock, 10000L, 10L);
        CashWallet cashWallet = createBuyerCashWallet(buyer, 100000L);

        ExchangeOrderResponse response = new ExchangeOrderResponse(
                MatchResult.CANCELLED,
                null,
                List.of(),
                0L,
                0L
        );

        given(orderRepository.findById(1L)).willReturn(Optional.of(buyOrder));
        given(cashWalletRepository.findByUserId(1L)).willReturn(Optional.of(cashWallet));

        // when
        orderResultService.handleExchangeOrderResponse(response, buyOrder.getId());

        // then
        assertThat(buyOrder.getUnfilledQuantity()).isZero();
        assertThat(cashWallet.getBalance()).isEqualTo(100000L);
        assertThat(cashWallet.getLockedAmount()).isZero();

        verify(cashWalletHistoryService).createCashWalletHistory(any());
        verifyNoInteractions(
                stockWalletRepository,
                matchRepository,
                marketStatusRepository,
                stockWalletHistoryService
        );
    }

    @Test
    void 거래소_응답이_CANCELLED이고_매도_주문이면_미체결_수량을_해제한다() {
        // given
        User seller = createUser(1L, "seller");
        Stock stock = createStock(1L);

        Order sellOrder = createSellOrder(1L, seller, stock, 10000L, 10L);
        StockWallet stockWallet = createSellerStockWallet(seller, stock, 10L);

        ExchangeOrderResponse response = new ExchangeOrderResponse(
                MatchResult.CANCELLED,
                null,
                List.of(),
                0L,
                0L
        );

        given(orderRepository.findById(1L)).willReturn(Optional.of(sellOrder));
        given(stockWalletRepository.findByUserIdAndStockId(1L, 1L))
                .willReturn(Optional.of(stockWallet));

        // when
        orderResultService.handleExchangeOrderResponse(response, sellOrder.getId());

        // then
        assertThat(sellOrder.getUnfilledQuantity()).isZero();
        assertThat(stockWallet.getHoldingQuantity()).isEqualTo(10L);
        assertThat(stockWallet.getLockedQuantity()).isZero();

        verify(stockWalletHistoryService).createStockWalletHistory(any());
        verifyNoInteractions(
                cashWalletRepository,
                matchRepository,
                marketStatusRepository,
                cashWalletHistoryService
        );
    }

    private User createUser(Long id, String name) {
        User user = User.signUp(
                name + "@test.com",
                "Password12!@",
                name
        );

        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Stock createStock(Long id) {
        Stock stock = new Stock("005930", "삼성전자");

        ReflectionTestUtils.setField(stock, "id", id);
        return stock;
    }

    private Order createBuyOrder(
            Long id,
            User user,
            Stock stock,
            long price,
            long quantity
    ) {
        Order order = Order.create(
                user,
                stock,
                BUY,
                price,
                quantity
        );

        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }

    private Order createSellOrder(
            Long id,
            User user,
            Stock stock,
            long price,
            long quantity
    ) {
        Order order = Order.create(
                user,
                stock,
                SELL,
                price,
                quantity
        );

        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }

    private CashWallet createBuyerCashWallet(
            User user,
            long amount
    ) {
        CashWallet cashWallet =
                CashWallet.create(user, "777123456781");

        cashWallet.deposit(amount);
        cashWallet.lock(amount);

        return cashWallet;
    }

    private CashWallet createSellerCashWallet(User user) {
        return CashWallet.create(user, "777876543211");
    }

    private StockWallet createBuyerStockWallet(
            User user,
            Stock stock
    ) {
        return StockWallet.create(user, stock);
    }

    private StockWallet createSellerStockWallet(
            User user,
            Stock stock,
            long quantity
    ) {
        StockWallet wallet =
                StockWallet.create(user, stock);

        wallet.credit(quantity);
        wallet.lock(quantity);

        return wallet;
    }

    private MarketStatus createMarketStatus(Stock stock) {
        return MarketStatus.create(
                stock,
                LocalDate.now(),
                new MarketPriceInfo(
                        10000L,
                        10500L,
                        9500L
                )
        );
    }

    private ExchangeOrderResponse createMatchedResponse(
            Long takerOrderId,
            Long makerOrderId,
            long price,
            long quantity
    ) {
        return new ExchangeOrderResponse(
                MatchResult.MATCHED,
                takerOrderId,
                List.of(
                        new MatchedMakerOrder(
                                makerOrderId,
                                quantity
                        )
                ),
                price,
                quantity
        );
    }
}