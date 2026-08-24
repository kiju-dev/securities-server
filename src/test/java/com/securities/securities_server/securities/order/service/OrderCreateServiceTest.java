package com.securities.securities_server.securities.order.service;

import com.securities.securities_server.securities.cashwallet.entity.CashWallet;
import com.securities.securities_server.securities.cashwallet.repository.CashWalletRepository;
import com.securities.securities_server.securities.market.entity.MarketStatus;
import com.securities.securities_server.securities.market.repository.MarketStatusRepository;
import com.securities.securities_server.securities.market.service.TickSizeCalculator;
import com.securities.securities_server.securities.order.controller.request.PlaceOrderRequest;
import com.securities.securities_server.securities.order.entity.Order;
import com.securities.securities_server.securities.order.repository.OrderRepository;
import com.securities.securities_server.securities.stock.entity.Stock;
import com.securities.securities_server.securities.stock.repository.StockRepository;
import com.securities.securities_server.securities.stockwallet.entity.StockWallet;
import com.securities.securities_server.securities.stockwallet.repository.StockWalletRepository;
import com.securities.securities_server.securities.user.entity.User;
import com.securities.securities_server.securities.user.repository.UserRepository;
import com.securities.securities_server.global.exception.CustomException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static com.securities.securities_server.global.common.OrderSide.BUY;
import static com.securities.securities_server.global.common.OrderSide.SELL;
import static com.securities.securities_server.global.exception.ErrorCode.INVALID_ORDER_PRICE;
import static com.securities.securities_server.global.exception.ErrorCode.STOCK_WALLET_SUSPENDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OrderCreateServiceTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    StockRepository stockRepository;
    @Mock
    CashWalletRepository cashWalletRepository;
    @Mock
    StockWalletRepository stockWalletRepository;
    @Mock
    MarketStatusRepository marketStatusRepository;
    @Mock
    TickSizeCalculator tickSizeCalculator;

    @InjectMocks
    OrderCreateService orderCreateService;

    @Test
    void 매수_주문_생성_시_현금_계좌_잔액을_lock하고_주문을_저장한다() {
        // given
        Long userId = 1L;
        Long stockId = 1L;

        User user = mock(User.class);
        Stock stock = mock(Stock.class);
        CashWallet cashWallet = mock(CashWallet.class);
        MarketStatus marketStatus = mock(MarketStatus.class);
        PlaceOrderRequest request = new PlaceOrderRequest(
                stockId,
                BUY,
                10000L,
                3L
        );
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(stockRepository.findById(stockId)).willReturn(Optional.of(stock));
        given(cashWalletRepository.findByUserId(userId)).willReturn(Optional.of(cashWallet));
        given(marketStatusRepository.findByStockIdAndTradingDate(eq(stockId), any(LocalDate.class)))
                .willReturn(Optional.of(marketStatus));
        given(marketStatus.getLowerLimitPrice()).willReturn(9500L);
        given(marketStatus.getUpperLimitPrice()).willReturn(10500L);
        given(tickSizeCalculator.validatePrice(10000L)).willReturn(true);

        // when
        orderCreateService.createOrder(userId, request);

        // then
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(captor.capture());

        Order savedOrder = captor.getValue();

        assertThat(savedOrder.getPrice()).isEqualTo(10000L);
        assertThat(savedOrder.getQuantity()).isEqualTo(3L);
        assertThat(savedOrder.getSide()).isEqualTo(BUY);
        assertThat(savedOrder.getUnfilledQuantity()).isEqualTo(3L);
        assertThat(savedOrder.getCanceledQuantity()).isEqualTo(0L);
        verify(cashWallet).lock(30000L);
    }

    @Test
    void 매도_주문_생성_시_종목_계좌_수량을_lock하고_주문을_저장한다() {
        // given
        Long userId = 1L;
        Long stockId = 1L;

        User user = mock(User.class);
        Stock stock = mock(Stock.class);
        CashWallet cashWallet = mock(CashWallet.class);
        StockWallet stockWallet = mock(StockWallet.class);
        MarketStatus marketStatus = mock(MarketStatus.class);
        PlaceOrderRequest request = new PlaceOrderRequest(
                stockId,
                SELL,
                10000L,
                3L
        );
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(stockRepository.findById(stockId)).willReturn(Optional.of(stock));
        given(cashWalletRepository.findByUserId(userId)).willReturn(Optional.of(cashWallet));
        given(stockWalletRepository.findByUserIdAndStockId(userId, stockId)).willReturn(Optional.of(stockWallet));
        given(marketStatusRepository.findByStockIdAndTradingDate(eq(stockId), any(LocalDate.class)))
                .willReturn(Optional.of(marketStatus));
        given(marketStatus.getLowerLimitPrice()).willReturn(9500L);
        given(marketStatus.getUpperLimitPrice()).willReturn(10500L);
        given(tickSizeCalculator.validatePrice(10000L)).willReturn(true);

        // when
        orderCreateService.createOrder(userId, request);

        // then
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);

        verify(orderRepository).save(captor.capture());

        Order savedOrder = captor.getValue();

        assertThat(savedOrder.getPrice()).isEqualTo(10000L);
        assertThat(savedOrder.getQuantity()).isEqualTo(3L);
        assertThat(savedOrder.getSide()).isEqualTo(SELL);
        assertThat(savedOrder.getUnfilledQuantity()).isEqualTo(3L);
        assertThat(savedOrder.getCanceledQuantity()).isEqualTo(0L);
        verify(stockWallet).lock(3L);
    }

    @Test
    void 주문_가격이_호가_단위에_맞지_않으면_INVALID_ORDER_PRICE_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long stockId = 1L;

        User user = mock(User.class);
        Stock stock = mock(Stock.class);
        CashWallet cashWallet = mock(CashWallet.class);
        StockWallet stockWallet = mock(StockWallet.class);
        MarketStatus marketStatus = mock(MarketStatus.class);
        PlaceOrderRequest request = new PlaceOrderRequest(
                stockId,
                SELL,
                10002L,
                3L
        );
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(stockRepository.findById(stockId)).willReturn(Optional.of(stock));
        given(cashWalletRepository.findByUserId(userId)).willReturn(Optional.of(cashWallet));
        given(stockWalletRepository.findByUserIdAndStockId(userId, stockId)).willReturn(Optional.of(stockWallet));
        given(marketStatusRepository.findByStockIdAndTradingDate(eq(stockId), any(LocalDate.class)))
                .willReturn(Optional.of(marketStatus));
        given(marketStatus.getLowerLimitPrice()).willReturn(9500L);
        given(marketStatus.getUpperLimitPrice()).willReturn(10500L);
        given(tickSizeCalculator.validatePrice(10002L)).willReturn(false);

        // when
        assertThatThrownBy(() -> orderCreateService.createOrder(userId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_ORDER_PRICE);
    }

    @Test
    void 주문_가격이_상하한가_범위를_벗어나면_INVALID_ORDER_PRICE_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long stockId = 1L;

        User user = mock(User.class);
        Stock stock = mock(Stock.class);
        CashWallet cashWallet = mock(CashWallet.class);
        StockWallet stockWallet = mock(StockWallet.class);
        MarketStatus marketStatus = mock(MarketStatus.class);
        PlaceOrderRequest request = new PlaceOrderRequest(
                stockId,
                SELL,
                14000L,
                3L
        );
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(stockRepository.findById(stockId)).willReturn(Optional.of(stock));
        given(cashWalletRepository.findByUserId(userId)).willReturn(Optional.of(cashWallet));
        given(stockWalletRepository.findByUserIdAndStockId(userId, stockId)).willReturn(Optional.of(stockWallet));
        given(marketStatusRepository.findByStockIdAndTradingDate(eq(stockId), any(LocalDate.class)))
                .willReturn(Optional.of(marketStatus));
        given(marketStatus.getLowerLimitPrice()).willReturn(9500L);
        given(marketStatus.getUpperLimitPrice()).willReturn(10500L);
        given(tickSizeCalculator.validatePrice(14000L)).willReturn(true);

        // when
        assertThatThrownBy(() -> orderCreateService.createOrder(userId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_ORDER_PRICE);
    }

    @Test
    void 매수_주문_시_종목_계좌가_정지_상태면_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long stockId = 1L;

        User user = mock(User.class);
        Stock stock = mock(Stock.class);
        CashWallet cashWallet = mock(CashWallet.class);
        StockWallet stockWallet = mock(StockWallet.class);
        PlaceOrderRequest request = new PlaceOrderRequest(
                stockId,
                BUY,
                10000L,
                3L
        );
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(stockRepository.findById(stockId)).willReturn(Optional.of(stock));
        given(cashWalletRepository.findByUserId(userId)).willReturn(Optional.of(cashWallet));
        given(stockWalletRepository.findByUserIdAndStockId(userId, stockId))
                .willReturn(Optional.of(stockWallet));
        willThrow(new CustomException(STOCK_WALLET_SUSPENDED))
                .given(stockWallet).validateNotBlocked();

        // when & then
        assertThatThrownBy(() -> orderCreateService.createOrder(userId, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(STOCK_WALLET_SUSPENDED);

        verify(orderRepository, never()).save(any(Order.class));
        verify(cashWallet, never()).lock(anyLong());
    }

    @Test
    void 매수_주문_시_종목_계좌가_없어도_주문이_생성된다() {
        // given
        Long userId = 1L;
        Long stockId = 1L;

        User user = mock(User.class);
        Stock stock = mock(Stock.class);
        CashWallet cashWallet = mock(CashWallet.class);
        MarketStatus marketStatus = mock(MarketStatus.class);
        PlaceOrderRequest request = new PlaceOrderRequest(
                stockId,
                BUY,
                10000L,
                3L
        );
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(stockRepository.findById(stockId)).willReturn(Optional.of(stock));
        given(cashWalletRepository.findByUserId(userId)).willReturn(Optional.of(cashWallet));
        given(stockWalletRepository.findByUserIdAndStockId(userId, stockId))
                .willReturn(Optional.empty());
        given(marketStatusRepository.findByStockIdAndTradingDate(eq(stockId), any(LocalDate.class)))
                .willReturn(Optional.of(marketStatus));
        given(marketStatus.getLowerLimitPrice()).willReturn(9500L);
        given(marketStatus.getUpperLimitPrice()).willReturn(10500L);
        given(tickSizeCalculator.validatePrice(10000L)).willReturn(true);

        // when
        orderCreateService.createOrder(userId, request);

        // then
        verify(orderRepository).save(any(Order.class));
        verify(cashWallet).lock(30000L);
    }

}