package com.securities.securities_server.domain.market.service.dto;

public record MarketPriceInfo(
        long referencePrice,
        long upperLimitPrice,
        long lowerLimitPrice
) {
}
