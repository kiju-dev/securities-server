package com.securities.securities_server.securities.order.controller.response;

import com.securities.securities_server.securities.order.entity.Order;
import com.securities.securities_server.securities.order.entity.OrderSide;

import java.time.LocalDateTime;

public record UnfilledOrder(
        Long stockId,
        Long orderId,
        OrderSide side,
        long price,
        long quantity,
        long unfilledQuantity,
        long canceledQuantity,
        LocalDateTime createdAt
) {
    public static UnfilledOrder from(Order order) {
        return new UnfilledOrder(
                order.getStock().getId(),
                order.getId(),
                order.getSide(),
                order.getPrice(),
                order.getQuantity(),
                order.getUnfilledQuantity(),
                order.getCanceledQuantity(),
                order.getCreatedAt()
        );
    }
}
