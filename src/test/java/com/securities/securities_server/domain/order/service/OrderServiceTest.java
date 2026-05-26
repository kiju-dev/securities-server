package com.securities.securities_server.domain.order.service;

import com.securities.securities_server.domain.order.controller.request.PlaceOrderRequest;
import com.securities.securities_server.domain.order.controller.response.CancelOrderResponse;
import com.securities.securities_server.domain.order.controller.response.PlaceOrderResponse;
import com.securities.securities_server.domain.order.entity.Order;
import com.securities.securities_server.domain.order.repository.OrderRepository;
import com.securities.securities_server.domain.user.entity.User;
import com.securities.securities_server.global.external.client.response.ExchangeOrderResponse;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.securities.securities_server.domain.order.entity.MatchResult.CANCELLED;
import static com.securities.securities_server.domain.order.entity.MatchResult.MATCHED;
import static com.securities.securities_server.domain.order.entity.OrderSide.BUY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OrderServiceTest {

    @Mock
    OrderCreateService orderCreateService;

    @Mock
    ExchangeOrderService exchangeOrderService;

    @Mock
    OrderResultService orderResultService;

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void 주문을_생성하고_거래소에_전송한_뒤_체결_결과를_반영하고_응답을_반환한다() {
        // given
        Long userId = 1L;
        Long orderId = 10L;

        PlaceOrderRequest request = new PlaceOrderRequest(
                1L,
                BUY,
                10000L,
                3L
        );

        Order order = mock(Order.class);
        given(order.getId()).willReturn(orderId);

        ExchangeOrderResponse exchangeOrderResponse = new ExchangeOrderResponse(
                MATCHED,
                orderId,
                List.of(),
                10000L,
                3L
        );

        given(orderCreateService.createOrder(userId, request)).willReturn(order);
        given(exchangeOrderService.sendOrder(orderId)).willReturn(exchangeOrderResponse);

        // when
        PlaceOrderResponse response = orderService.placeOrder(userId, request);

        // then
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.matchResult()).isEqualTo(MATCHED);

        verify(orderCreateService).createOrder(userId, request);
        verify(exchangeOrderService).sendOrder(orderId);
        verify(orderResultService).handleExchangeOrderResponse(exchangeOrderResponse, orderId);
    }

    @Test
    void 거래소에_주문_취소_요청을_전송한_뒤_결과를_반영하고_응답을_반환한다() {
        // given
        Long userId = 1L;
        Long orderId = 10L;

        Order order = mock(Order.class);
        User user = mock(User.class);

        given(order.getUser()).willReturn(user);
        given(user.getId()).willReturn(userId);
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        ExchangeOrderResponse exchangeOrderResponse = new ExchangeOrderResponse(
                CANCELLED,
                null,
                List.of(),
                0L,
                0L
        );

        given(exchangeOrderService.cancelOrder(orderId)).willReturn(exchangeOrderResponse);

        // when
        CancelOrderResponse response = orderService.cancelOrder(userId, orderId);

        // then
        assertThat(response.orderId()).isEqualTo(orderId);
        assertThat(response.matchResult()).isEqualTo(CANCELLED);

        verify(orderRepository).findById(orderId);
        verify(exchangeOrderService).cancelOrder(orderId);
        verify(orderResultService).handleExchangeOrderResponse(exchangeOrderResponse, orderId);
    }
}