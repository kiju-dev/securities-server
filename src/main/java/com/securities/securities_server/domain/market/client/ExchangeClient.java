package com.securities.securities_server.domain.market.client;

import com.securities.securities_server.domain.market.controller.response.MarketOpenResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "exchange",
        url = "${exchange.url}"
)
public interface ExchangeClient {

    @PostMapping("/market/open")
    MarketOpenResponse openMarket();
}
