package com.securities.securities_server.domain.orderbook.controller.response;

import java.util.List;

public record PriceLevel(
        long totalQuantity,
        List<OrderSummary> orders
) {
}
