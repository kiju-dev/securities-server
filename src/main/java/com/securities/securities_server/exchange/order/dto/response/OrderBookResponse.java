package com.securities.securities_server.exchange.order.dto.response;

import java.util.Map;

public record OrderBookResponse(
        Map<Long, PriceLevel> buy,
        Map<Long, PriceLevel> sell
) {
}
