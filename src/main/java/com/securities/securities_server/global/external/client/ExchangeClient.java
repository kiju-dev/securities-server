package com.securities.securities_server.global.external.client;

import com.securities.securities_server.domain.market.controller.response.MarketCloseResponse;
import com.securities.securities_server.domain.market.controller.response.MarketOpenResponse;
import com.securities.securities_server.global.external.client.request.ExchangeCancelRequest;
import com.securities.securities_server.global.external.client.request.ExchangeOrderRequest;
import com.securities.securities_server.global.external.client.response.ExchangeOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "exchange",
        url = "${exchange.url}"
)
public interface ExchangeClient {

    @PostMapping("/market/open")
    MarketOpenResponse openMarket();

    @PostMapping("/market/close")
    MarketCloseResponse closeMarket();

    @PostMapping("/market/order")
    ExchangeOrderResponse order(ExchangeOrderRequest request);

    @DeleteMapping("/market/order")
    ExchangeOrderResponse cancel(ExchangeCancelRequest request);
}
