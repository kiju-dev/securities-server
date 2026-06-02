package com.securities.securities_server.exchange.order.dto.request;

import com.securities.securities_server.securities.order.entity.Order;
import com.securities.securities_server.securities.order.entity.OrderSide;

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
