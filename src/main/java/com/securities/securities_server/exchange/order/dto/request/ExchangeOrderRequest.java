package com.securities.securities_server.exchange.order.dto.request;

import com.securities.securities_server.global.common.OrderSide;

import java.time.LocalDateTime;

public record ExchangeOrderRequest(
        Long orderId,
        Long userId,
        Long stockId,
        long price,
        long quantity,
        OrderSide side,
        LocalDateTime createdAt
) {
}
