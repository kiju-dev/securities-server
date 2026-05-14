package com.securities.securities_server.domain.market.controller;

import com.securities.securities_server.domain.market.controller.response.MarketCloseResponse;
import com.securities.securities_server.domain.market.controller.response.MarketOpenResponse;
import com.securities.securities_server.domain.market.service.MarketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/market")
public class MarketController {

    private final MarketService marketService;

    @PostMapping("/open")
    public ResponseEntity<MarketOpenResponse> openMarket() {
        MarketOpenResponse response = marketService.openMarket();
        return ResponseEntity.status(OK).body(response);
    }

    @PostMapping("/close")
    public ResponseEntity<MarketCloseResponse> closeMarket() {
        MarketCloseResponse response = marketService.closeMarket();
        return ResponseEntity.status(OK).body(response);
    }
}
