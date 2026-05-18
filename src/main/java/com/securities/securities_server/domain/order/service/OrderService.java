package com.securities.securities_server.domain.order.service;

import com.securities.securities_server.domain.order.controller.request.PlaceOrderRequest;
import com.securities.securities_server.domain.order.controller.response.PlaceOrderResponse;
import com.securities.securities_server.domain.order.entity.Order;
import com.securities.securities_server.global.external.client.response.ExchangeOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderCreateService orderCreateService;
    private final ExchangeOrderService exchangeOrderService;
    private final OrderResultService orderResultService;

    public PlaceOrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        Order order = orderCreateService.createOrder(userId, request);

        ExchangeOrderResponse exchangeOrderResponse = exchangeOrderService.sendOrder(order.getId());

        orderResultService.handleExchangeOrderResponse(exchangeOrderResponse, request.side());
        return new PlaceOrderResponse(order.getId(), exchangeOrderResponse.matchResult());
    }
}
