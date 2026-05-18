package com.securities.securities_server.domain.order.service;

import com.securities.securities_server.domain.order.controller.request.PlaceOrderRequest;
import com.securities.securities_server.domain.order.controller.response.PlaceOrderResponse;
import com.securities.securities_server.domain.order.entity.Order;
import com.securities.securities_server.global.external.client.response.ExchangeOrderResponse;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
        assertThat(response.result()).isEqualTo(MATCHED);

        verify(orderCreateService).createOrder(userId, request);
        verify(exchangeOrderService).sendOrder(orderId);
        verify(orderResultService).handleExchangeOrderResponse(exchangeOrderResponse, BUY);
    }
}