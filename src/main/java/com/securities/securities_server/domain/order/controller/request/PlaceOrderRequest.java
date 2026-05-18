package com.securities.securities_server.domain.order.controller.request;

import com.securities.securities_server.domain.order.entity.OrderSide;

public record PlaceOrderRequest(
        Long stockId,
        OrderSide side,
        long price,
        long quantity
) {
}
