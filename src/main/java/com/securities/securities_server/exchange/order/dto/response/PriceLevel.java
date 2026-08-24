package com.securities.securities_server.exchange.order.dto.response;

import java.util.List;

public record PriceLevel(
        long totalQuantity,
        List<OrderSummary> orders
) {
}
