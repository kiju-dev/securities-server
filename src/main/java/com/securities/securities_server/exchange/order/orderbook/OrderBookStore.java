package com.securities.securities_server.exchange.order.orderbook;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OrderBookStore {

    private final Map<Long, OrderBook> orderBooks = new ConcurrentHashMap<>();

    public OrderBook getOrderBook(Long stockId) {
        return orderBooks.computeIfAbsent(stockId, id -> new OrderBook());
    }

    public void closeAll() {
        for (OrderBook orderBook : orderBooks.values()) {
            orderBook.close();
        }
    }
}
