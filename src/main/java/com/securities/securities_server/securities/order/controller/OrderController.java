package com.securities.securities_server.securities.order.controller;

import com.securities.securities_server.securities.order.controller.request.PlaceOrderRequest;
import com.securities.securities_server.securities.order.controller.response.CancelOrderResponse;
import com.securities.securities_server.securities.order.controller.response.PlaceOrderResponse;
import com.securities.securities_server.securities.order.controller.response.UnfilledOrderResponse;
import com.securities.securities_server.global.common.OrderSide;
import com.securities.securities_server.securities.order.service.OrderService;
import com.securities.securities_server.global.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @DeleteMapping("/order/{orderId}")
    public ResponseEntity<CancelOrderResponse> cancelOrder(
            @AuthUser Long userId,
            @PathVariable Long orderId
    ) {
        CancelOrderResponse response = orderService.cancelOrder(userId, orderId);
        return ResponseEntity.status(OK).body(response);
    }

    @GetMapping("/order/unfilled")
    public ResponseEntity<UnfilledOrderResponse> getUnfilledOrder(
            @AuthUser Long userId,
            @RequestParam Long stockId,
            @RequestParam OrderSide side,
            Pageable pageable
    ) {
        UnfilledOrderResponse response = orderService.getUnfilledOrder(userId, stockId, side, pageable);
        return ResponseEntity.status(OK).body(response);
    }
}
