package com.securities.securities_server.securities.market.service.dto;

public record MarketPriceInfo(
        long referencePrice,
        long upperLimitPrice,
        long lowerLimitPrice
) {
}
