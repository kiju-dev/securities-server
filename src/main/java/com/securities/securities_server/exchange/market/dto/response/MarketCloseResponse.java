package com.securities.securities_server.exchange.market.dto.response;

import com.securities.securities_server.global.common.ExchangeStatus;

import java.time.LocalDateTime;

public record MarketCloseResponse(
    ExchangeStatus exchangeStatus,
    LocalDateTime closedAt
) {
}
