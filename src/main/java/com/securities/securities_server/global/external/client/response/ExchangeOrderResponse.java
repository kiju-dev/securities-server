package com.securities.securities_server.global.external.client.response;

import com.securities.securities_server.domain.order.entity.MatchResult;

import java.util.List;

public record ExchangeOrderResponse(
        MatchResult matchResult,
        Long takerOrderId,
        List<MakerOrderList> makers,
        long price,
        long totalMatchedQuantity
) {
}
