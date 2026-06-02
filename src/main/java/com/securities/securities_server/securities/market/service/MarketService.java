package com.securities.securities_server.securities.market.service;

import com.securities.securities_server.exchange.market.service.ExchangeMarketService;
import com.securities.securities_server.securities.market.controller.response.MarketCloseResponse;
import com.securities.securities_server.securities.market.controller.response.MarketOpenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final MarketStatusService marketStatusService;
    private final ExchangeMarketService exchangeMarketService;

    public MarketOpenResponse openMarket() {
        return exchangeMarketService.openMarket();
    }

    public MarketCloseResponse closeMarket() {
        MarketCloseResponse marketCloseResponse = exchangeMarketService.closeMarket();
        marketStatusService.createNextDayMarketStatuses();
        return marketCloseResponse;
    }
}
