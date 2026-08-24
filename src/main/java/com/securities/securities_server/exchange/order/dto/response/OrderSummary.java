package com.securities.securities_server.exchange.order.dto.response;

import java.time.LocalDateTime;

public record OrderSummary(
        Long orderId,
        long unfilledQuantity,
        LocalDateTime createdAt
) {
}
