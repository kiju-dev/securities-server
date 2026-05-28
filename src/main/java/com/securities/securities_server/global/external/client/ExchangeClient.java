package com.securities.securities_server.global.external.client;

import com.securities.securities_server.domain.market.controller.response.MarketCloseResponse;
import com.securities.securities_server.domain.market.controller.response.MarketOpenResponse;
import com.securities.securities_server.domain.orderbook.controller.response.OrderBookResponse;
import com.securities.securities_server.global.external.client.request.ExchangeCancelRequest;
import com.securities.securities_server.global.external.client.request.ExchangeOrderRequest;
import com.securities.securities_server.global.external.client.response.ExchangeOrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "exchange",
        url = "${exchange.url}"
)
public interface ExchangeClient {

    @PostMapping("/api/v1/market/open")
    MarketOpenResponse openMarket();

    @PostMapping("/api/v1/market/close")
    MarketCloseResponse closeMarket();

    @PostMapping("/api/v1/market/order")
    ExchangeOrderResponse order(ExchangeOrderRequest request);

    @DeleteMapping("/api/v1/market/order")
    ExchangeOrderResponse cancel(ExchangeCancelRequest request);

    @GetMapping("/api/v1/market/orderbook/{stockId}")
    OrderBookResponse getOrderBook(@PathVariable Long stockId);
}
