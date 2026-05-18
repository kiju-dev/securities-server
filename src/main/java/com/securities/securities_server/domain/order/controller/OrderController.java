package com.securities.securities_server.domain.order.controller;

import com.securities.securities_server.domain.order.controller.request.PlaceOrderRequest;
import com.securities.securities_server.domain.order.controller.response.PlaceOrderResponse;
import com.securities.securities_server.domain.order.service.OrderService;
import com.securities.securities_server.global.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<PlaceOrderResponse> placeOrder(
            @AuthUser Long userId,
            @RequestBody PlaceOrderRequest request
    ) {
        PlaceOrderResponse response = orderService.placeOrder(userId, request);
        return ResponseEntity.status(OK).body(response);
    }
}
