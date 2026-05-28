package com.securities.securities_server.domain.orderbook.controller.response;

import java.util.Map;

public record OrderBookResponse(
        Map<Long, PriceLevel> buy,
        Map<Long, PriceLevel> sell
) {
}
