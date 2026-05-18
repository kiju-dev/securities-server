package com.securities.securities_server.domain.order.service;

import com.securities.securities_server.domain.order.entity.Order;
import com.securities.securities_server.domain.order.repository.OrderRepository;
import com.securities.securities_server.global.exception.CustomException;
import com.securities.securities_server.global.external.client.ExchangeClient;
import com.securities.securities_server.global.external.client.request.ExchangeOrderRequest;
import com.securities.securities_server.global.external.client.response.ExchangeOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.securities.securities_server.global.exception.ErrorCode.ORDER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ExchangeOrderService {

    private final ExchangeClient exchangeClient;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public ExchangeOrderResponse sendOrder(Long orderId) {
        Order order = getOrder(orderId);

        ExchangeOrderRequest exchangeOrderRequest = ExchangeOrderRequest.from(order);
        return exchangeClient.order(exchangeOrderRequest);
    }

    private Order getOrder(Long orderId){
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));
    }
}
