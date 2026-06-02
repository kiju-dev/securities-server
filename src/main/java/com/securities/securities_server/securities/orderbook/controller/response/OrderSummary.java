package com.securities.securities_server.securities.orderbook.controller.response;

import java.time.LocalDateTime;

public record OrderSummary(
        Long orderId,
        long unfilledQuantity,
        LocalDateTime createdAt
) {
}
