package com.securities.securities_server.exchange.order.service;

import com.securities.securities_server.exchange.market.ExchangeState;
import com.securities.securities_server.exchange.order.dto.request.ExchangeCancelRequest;
import com.securities.securities_server.exchange.order.dto.request.ExchangeOrderRequest;
import com.securities.securities_server.exchange.order.dto.response.ExchangeOrderResponse;
import com.securities.securities_server.exchange.order.orderbook.OrderBook;
import com.securities.securities_server.exchange.order.orderbook.OrderBookStore;
import com.securities.securities_server.exchange.order.dto.response.OrderBookResponse;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.securities.securities_server.global.common.MatchResult.CANCELLED;
import static com.securities.securities_server.global.common.MatchResult.UNMATCHED;
import static com.securities.securities_server.global.common.OrderSide.BUY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ExchangeOrderServiceTest {

    @Mock
    private ExchangeState exchangeState;

    @Mock
    private OrderBookStore orderBookStore;

    @Mock
    private OrderBook orderBook;

    @InjectMocks
    private ExchangeOrderService exchangeOrderService;

    @Test
    void 주문_요청_시_장_상태를_검증하고_해당_종목의_OrderBook에_주문을_넣는다() {
        // given
        ExchangeOrderRequest request = new ExchangeOrderRequest(
                1L,
                1L,
                10L,
                10_000L,
                5L,
                BUY,
                LocalDateTime.of(2026, 5, 28, 10, 0)
        );

        ExchangeOrderResponse expectedResponse =
                new ExchangeOrderResponse(UNMATCHED, null, List.of(), 0L, 0L);

        given(orderBookStore.getOrderBook(request.stockId())).willReturn(orderBook);
        given(orderBook.place(org.mockito.ArgumentMatchers.any())).willReturn(expectedResponse);

        // when
        ExchangeOrderResponse response = exchangeOrderService.placeOrder(request);

        // then
        assertThat(response).isEqualTo(expectedResponse);

        verify(exchangeState).validateRunning();
        verify(orderBookStore).getOrderBook(10L);

        ArgumentCaptor<com.securities.securities_server.exchange.order.ExchangeOrder> captor =
                ArgumentCaptor.forClass(com.securities.securities_server.exchange.order.ExchangeOrder.class);

        verify(orderBook).place(captor.capture());

        assertThat(captor.getValue().getOrderId()).isEqualTo(1L);
        assertThat(captor.getValue().getStockId()).isEqualTo(10L);
        assertThat(captor.getValue().getPrice()).isEqualTo(10_000L);
        assertThat(captor.getValue().getRemainingQuantity()).isEqualTo(5L);
        assertThat(captor.getValue().getSide()).isEqualTo(BUY);
    }

    @Test
    void 주문_취소_요청_시_장_상태를_검증하고_해당_종목의_OrderBook에서_주문을_취소한다() {
        // given
        ExchangeCancelRequest request = new ExchangeCancelRequest(
                1L,
                10L,
                BUY,
                10_000L
        );

        ExchangeOrderResponse expectedResponse =
                new ExchangeOrderResponse(CANCELLED, null, List.of(), 0L, 0L);

        given(orderBookStore.getOrderBook(request.stockId())).willReturn(orderBook);
        given(orderBook.cancel(request.orderId())).willReturn(expectedResponse);

        // when
        ExchangeOrderResponse response = exchangeOrderService.cancelOrder(request);

        // then
        assertThat(response).isEqualTo(expectedResponse);

        verify(exchangeState).validateRunning();
        verify(orderBookStore).getOrderBook(10L);
        verify(orderBook).cancel(1L);
    }

    @Test
    void 호가창_조회_시_장_상태를_검증하고_해당_종목의_OrderBook을_조회한다() {
        // given
        Long stockId = 10L;

        OrderBookResponse expectedResponse = new OrderBookResponse(
                Map.of(),
                Map.of()
        );

        given(orderBookStore.getOrderBook(stockId)).willReturn(orderBook);
        given(orderBook.getOrderBook()).willReturn(expectedResponse);

        // when
        OrderBookResponse response = exchangeOrderService.getOrderBook(stockId);

        // then
        assertThat(response).isEqualTo(expectedResponse);

        verify(exchangeState).validateRunning();
        verify(orderBookStore).getOrderBook(stockId);
        verify(orderBook).getOrderBook();
    }
}