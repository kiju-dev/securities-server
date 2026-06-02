package com.securities.securities_server.exchange.order.dto.request;

import com.securities.securities_server.securities.order.controller.request.PlaceOrderRequest;
import com.securities.securities_server.securities.order.entity.Order;
import com.securities.securities_server.securities.order.entity.OrderSide;

import java.time.LocalDateTime;

public record ExchangeOrderRequest(
        Long orderId,
        Long stockId,
        long price,
        long quantity,
        OrderSide side,
        LocalDateTime createdAt
) {
    public static ExchangeOrderRequest of(Order order, PlaceOrderRequest request) {
        return new ExchangeOrderRequest(
                order.getId(),
                request.stockId(),
                request.price(),
                request.quantity(),
                request.side(),
                order.getCreatedAt()
        );
    }
}
