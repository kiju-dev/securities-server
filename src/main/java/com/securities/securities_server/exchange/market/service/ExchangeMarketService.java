package com.securities.securities_server.exchange.market.service;

import com.securities.securities_server.exchange.market.ExchangeState;
import com.securities.securities_server.exchange.order.orderbook.OrderBookStore;
import com.securities.securities_server.securities.market.controller.response.MarketCloseResponse;
import com.securities.securities_server.securities.market.controller.response.MarketOpenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ExchangeMarketService {

    private final ExchangeState exchangeState;
    private final OrderBookStore orderBookStore;

    public MarketOpenResponse openMarket() {
        exchangeState.open();
        return new MarketOpenResponse(exchangeState.getStatus(), LocalDateTime.now());
    }

    public MarketCloseResponse closeMarket() {
        exchangeState.close();
        orderBookStore.closeAll();
        return new MarketCloseResponse(exchangeState.getStatus(), LocalDateTime.now());
    }
}
