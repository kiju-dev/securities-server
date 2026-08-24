package com.securities.securities_server.exchange.order.dto.request;

import com.securities.securities_server.global.common.OrderSide;

public record ExchangeCancelRequest(
        Long orderId,
        Long stockId,
        OrderSide side,
        long price
) {
}
