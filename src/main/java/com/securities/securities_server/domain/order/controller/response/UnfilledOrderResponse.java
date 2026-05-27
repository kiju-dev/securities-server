package com.securities.securities_server.domain.order.controller.response;

import java.util.List;

public record UnfilledOrderResponse(
        long totalElements,
        List<UnfilledOrder> unfilledOrderList
) {
}
