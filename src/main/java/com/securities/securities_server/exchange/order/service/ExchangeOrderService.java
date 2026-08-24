package com.securities.securities_server.exchange.order.service;

import com.securities.securities_server.exchange.market.ExchangeState;
import com.securities.securities_server.exchange.order.ExchangeOrder;
import com.securities.securities_server.exchange.order.dto.request.ExchangeCancelRequest;
import com.securities.securities_server.exchange.order.dto.request.ExchangeOrderRequest;
import com.securities.securities_server.exchange.order.dto.response.ExchangeOrderResponse;
import com.securities.securities_server.exchange.order.orderbook.OrderBook;
import com.securities.securities_server.exchange.order.orderbook.OrderBookStore;

import com.securities.securities_server.exchange.order.dto.response.OrderBookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeOrderService {

    private final ExchangeState exchangeState;
    private final OrderBookStore orderBookStore;

    public ExchangeOrderResponse placeOrder(ExchangeOrderRequest request) {
        exchangeState.validateRunning();

        OrderBook orderBook = orderBookStore.getOrderBook(request.stockId());
        return orderBook.place(ExchangeOrder.from(request));
    }

    public ExchangeOrderResponse cancelOrder(ExchangeCancelRequest request) {
        exchangeState.validateRunning();

        OrderBook orderBook = orderBookStore.getOrderBook(request.stockId());
        return orderBook.cancel(request.orderId());
    }

    public OrderBookResponse getOrderBook(Long stockId) {
        exchangeState.validateRunning();

        OrderBook orderBook = orderBookStore.getOrderBook(stockId);
        return orderBook.getOrderBook();
    }
}
