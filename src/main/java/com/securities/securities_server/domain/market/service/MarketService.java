package com.securities.securities_server.domain.market.service;

import com.securities.securities_server.domain.market.client.ExchangeClient;
import com.securities.securities_server.domain.market.controller.response.MarketCloseResponse;
import com.securities.securities_server.domain.market.controller.response.MarketOpenResponse;
import com.securities.securities_server.global.exception.CustomException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.securities.securities_server.global.exception.ErrorCode.EXCHANGE_SERVER_ERROR;
import static com.securities.securities_server.global.exception.ErrorCode.MARKET_ALREADY_CLOSE;
import static com.securities.securities_server.global.exception.ErrorCode.MARKET_ALREADY_OPEN;

@Service
@RequiredArgsConstructor
public class MarketService {

    private final ExchangeClient exchangeClient;
    private final MarketStatusService marketStatusService;

    public MarketOpenResponse openMarket() {
        try {
            return exchangeClient.openMarket();
        } catch (FeignException e) {
            if (e.status() == 409) {
                throw new CustomException(MARKET_ALREADY_OPEN);
            }
            throw new CustomException(EXCHANGE_SERVER_ERROR);
        }
    }

    public MarketCloseResponse closeMarket() {
        try {
            MarketCloseResponse response = exchangeClient.closeMarket();
            marketStatusService.createNextDayMarketStatuses();
            return response;
        } catch (FeignException e) {
            if (e.status() == 409) {
                throw new CustomException(MARKET_ALREADY_CLOSE);
            }
            throw new CustomException(EXCHANGE_SERVER_ERROR);
        }
    }
}
