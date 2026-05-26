package com.securities.securities_server.global.external.client.request;

import com.securities.securities_server.domain.order.entity.Order;
import com.securities.securities_server.domain.order.entity.OrderSide;

public record ExchangeCancelRequest(
        Long orderId,
        Long stockId,
        OrderSide side,
        long price
) {
    public static ExchangeCancelRequest from(Order order) {
        return new ExchangeCancelRequest(
                order.getId(),
                order.getStock().getId(),
                order.getSide(),
                order.getPrice()
        );
    }
}
