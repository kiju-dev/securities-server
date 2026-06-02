package com.securities.securities_server.securities.order.controller.response;

import com.securities.securities_server.securities.order.entity.MatchResult;

public record PlaceOrderResponse(
        Long orderId,
        MatchResult matchResult
) {
}
