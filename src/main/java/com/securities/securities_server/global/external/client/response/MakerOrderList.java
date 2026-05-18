package com.securities.securities_server.global.external.client.response;

public record MakerOrderList(
        Long orderId,
        long matchedQuantity
) {
}
