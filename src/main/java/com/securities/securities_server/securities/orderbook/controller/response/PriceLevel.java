package com.securities.securities_server.securities.orderbook.controller.response;

import java.util.List;

public record PriceLevel(
        long totalQuantity,
        List<OrderSummary> orders
) {
}
