package com.securities.securities_server.domain.order.controller.response;

import com.securities.securities_server.domain.order.entity.MatchResult;

public record CancelOrderResponse(
        Long orderId,
        MatchResult matchResult
) {
}
