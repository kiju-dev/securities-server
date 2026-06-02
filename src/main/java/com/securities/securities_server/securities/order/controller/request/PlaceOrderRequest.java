package com.securities.securities_server.securities.order.controller.request;

import com.securities.securities_server.securities.order.entity.OrderSide;

public record PlaceOrderRequest(
        Long stockId,
        OrderSide side,
        long price,
        long quantity
) {
}
