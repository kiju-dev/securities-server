package com.securities.securities_server.securities.order.controller.response;

import com.securities.securities_server.global.common.MatchResult;

public record CancelOrderResponse(
        Long orderId,
        MatchResult matchResult
) {
}
