package com.securities.securities_server.domain.orderbook.controller.response;

import java.util.List;

public record OrderBookResponse(
        List<PriceLevel> buy,
        List<PriceLevel> sell
) {
}
