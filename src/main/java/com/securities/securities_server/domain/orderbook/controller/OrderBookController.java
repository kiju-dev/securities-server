package com.securities.securities_server.domain.orderbook.controller;

import com.securities.securities_server.domain.orderbook.controller.response.OrderBookResponse;
import com.securities.securities_server.domain.orderbook.service.OrderBookService;
import com.securities.securities_server.global.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
public class OrderBookController {

    private final OrderBookService orderBookService;

    @GetMapping("/api/v1/orderbook/{stockId}")
    public ResponseEntity<OrderBookResponse> getOrderBook(
            @AuthUser Long userId,
            @PathVariable Long stockId
    ) {
        OrderBookResponse response = orderBookService.getOrderBook(stockId);
        return ResponseEntity.status(OK).body(response);
    }
}
