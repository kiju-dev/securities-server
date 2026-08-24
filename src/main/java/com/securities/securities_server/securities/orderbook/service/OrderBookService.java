package com.securities.securities_server.securities.orderbook.service;

import com.securities.securities_server.exchange.order.service.ExchangeOrderService;
import com.securities.securities_server.exchange.order.dto.response.OrderBookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderBookService {

    private final ExchangeOrderService exchangeOrderService;

    public OrderBookResponse getOrderBook(Long stockId) {
        return exchangeOrderService.getOrderBook(stockId);
    }
}
