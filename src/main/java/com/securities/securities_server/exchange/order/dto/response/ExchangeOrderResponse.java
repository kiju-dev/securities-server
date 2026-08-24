package com.securities.securities_server.exchange.order.dto.response;

import com.securities.securities_server.global.common.MatchResult;

import java.util.List;

public record ExchangeOrderResponse(
        MatchResult matchResult,
        Long takerOrderId,
        List<MatchedMakerOrder> makers,
        long price,
        long totalMatchedQuantity
) {
}
