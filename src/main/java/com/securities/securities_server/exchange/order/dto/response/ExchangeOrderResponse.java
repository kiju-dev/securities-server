package com.securities.securities_server.exchange.order.dto.response;

import com.securities.securities_server.securities.order.entity.MatchResult;

import java.util.List;

public record ExchangeOrderResponse(
        MatchResult matchResult,
        Long takerOrderId,
        List<MatchedMakerOrder> makers,
        long price,
        long totalMatchedQuantity
) {
}
