package com.securities.securities_server.exchange.order;

import com.securities.securities_server.exchange.order.dto.request.ExchangeOrderRequest;
import com.securities.securities_server.securities.order.entity.OrderSide;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ExchangeOrder {

    private Long orderId;
    private Long stockId;
    private long price;
    private long quantity;
    private OrderSide side;
    private LocalDateTime createdAt;
    private long remainingQuantity;

    private ExchangeOrder(
            Long orderId,
            Long stockId,
            long price,
            long quantity,
            OrderSide side,
            LocalDateTime createdAt
    ) {
        this.orderId = orderId;
        this.stockId = stockId;
        this.price = price;
        this.quantity = quantity;
        this.side = side;
        this.createdAt = createdAt;
        this.remainingQuantity = quantity;
    }

    public static ExchangeOrder from(ExchangeOrderRequest request) {
        return new ExchangeOrder(
                request.orderId(),
                request.stockId(),
                request.price(),
                request.quantity(),
                request.side(),
                request.createdAt()
        );
    }

    public void decreaseQuantity(long quantity) {
        this.remainingQuantity -= quantity;
    }
}
