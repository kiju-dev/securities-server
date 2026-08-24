package com.securities.securities_server.exchange.order.dto.response;

public record MatchedMakerOrder(
        Long orderId,
        long matchedQuantity
) {
}
