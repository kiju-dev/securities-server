package com.securities.securities_server.global.external.client.request;

import com.securities.securities_server.domain.order.entity.Order;
import com.securities.securities_server.domain.order.entity.OrderSide;

import java.time.LocalDateTime;

public record ExchangeOrderRequest(
        Long orderId,
        Long stockId,
        long price,
        long quantity,
        OrderSide side,
        LocalDateTime createdAt
) {
    public static ExchangeOrderRequest from(Order order) {
        return new ExchangeOrderRequest(
                order.getId(),
                order.getStock().getId(),
                order.getPrice(),
                order.getQuantity(),
                order.getSide(),
                order.getCreatedAt()
        );
    }
}
