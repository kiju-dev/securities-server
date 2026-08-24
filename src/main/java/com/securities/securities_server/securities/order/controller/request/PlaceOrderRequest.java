package com.securities.securities_server.securities.order.controller.request;

import com.securities.securities_server.global.common.OrderSide;

public record PlaceOrderRequest(
        Long stockId,
        OrderSide side,
        long price,
        long quantity
) {
}
