package com.securities.securities_server.domain.orderbook.service;

import com.securities.securities_server.domain.orderbook.controller.response.OrderBookResponse;
import com.securities.securities_server.global.exception.CustomException;
import com.securities.securities_server.global.external.client.ExchangeClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.securities.securities_server.global.exception.ErrorCode.EXCHANGE_SERVER_ERROR;

@Service
@RequiredArgsConstructor
public class OrderBookService {

    private final ExchangeClient exchangeClient;

    public OrderBookResponse getOrderBook(Long stockId) {
        try {
            return exchangeClient.getOrderBook(stockId);
        } catch (FeignException e) {
            throw new CustomException(EXCHANGE_SERVER_ERROR);
        }
    }
}
