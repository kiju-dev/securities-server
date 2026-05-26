package com.securities.securities_server.domain.order.service;

import com.securities.securities_server.domain.order.controller.request.PlaceOrderRequest;
import com.securities.securities_server.domain.order.controller.response.CancelOrderResponse;
import com.securities.securities_server.domain.order.controller.response.PlaceOrderResponse;
import com.securities.securities_server.domain.order.entity.Order;
import com.securities.securities_server.domain.order.repository.OrderRepository;
import com.securities.securities_server.global.exception.CustomException;
import com.securities.securities_server.global.external.client.response.ExchangeOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.securities.securities_server.global.exception.ErrorCode.FORBIDDEN_ORDER_ACCESS;
import static com.securities.securities_server.global.exception.ErrorCode.ORDER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderCreateService orderCreateService;
    private final ExchangeOrderService exchangeOrderService;
    private final OrderResultService orderResultService;

    public PlaceOrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        Order order = orderCreateService.createOrder(userId, request);

        ExchangeOrderResponse exchangeOrderResponse = exchangeOrderService.sendOrder(order.getId());

        orderResultService.handleExchangeOrderResponse(exchangeOrderResponse, order.getId());
        return new PlaceOrderResponse(order.getId(), exchangeOrderResponse.matchResult());
    }

    public CancelOrderResponse cancelOrder(Long userId, Long orderId) {
        validateOwner(userId, orderId);
        ExchangeOrderResponse exchangeOrderResponse = exchangeOrderService.cancelOrder(orderId);

        orderResultService.handleExchangeOrderResponse(exchangeOrderResponse, orderId);
        return new CancelOrderResponse(orderId, exchangeOrderResponse.matchResult());
    }

    private void validateOwner(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));
        if (!order.getUser().getId().equals(userId)) {
            throw new CustomException(FORBIDDEN_ORDER_ACCESS);
        }
    }
}
