package com.securities.securities_server.securities.market.controller.response;

import com.securities.securities_server.securities.market.entity.ExchangeStatus;

import java.time.LocalDateTime;

public record MarketCloseResponse(
    ExchangeStatus exchangeStatus,
    LocalDateTime closedAt
) {
}
