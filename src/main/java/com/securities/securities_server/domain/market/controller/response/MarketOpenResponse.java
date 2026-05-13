package com.securities.securities_server.domain.market.controller.response;

import com.securities.securities_server.domain.market.entity.ExchangeStatus;

import java.time.LocalDateTime;

public record MarketOpenResponse(
    ExchangeStatus exchangeStatus,
    LocalDateTime openedAt
) {
}
