package com.securities.securities_server.securities.order.service;

import com.securities.securities_server.exchange.order.dto.request.ExchangeCancelRequest;
import com.securities.securities_server.exchange.order.dto.request.ExchangeOrderRequest;
import com.securities.securities_server.exchange.order.dto.response.ExchangeOrderResponse;
import com.securities.securities_server.exchange.order.service.ExchangeOrderService;
import com.securities.securities_server.securities.order.controller.request.PlaceOrderRequest;
import com.securities.securities_server.securities.order.controller.response.CancelOrderResponse;
import com.securities.securities_server.securities.order.controller.response.PlaceOrderResponse;
import com.securities.securities_server.securities.order.controller.response.UnfilledOrder;
import com.securities.securities_server.securities.order.controller.response.UnfilledOrderResponse;
import com.securities.securities_server.securities.order.entity.Order;
import com.securities.securities_server.global.common.OrderSide;
import com.securities.securities_server.securities.order.repository.OrderRepository;
import com.securities.securities_server.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.securities.securities_server.global.exception.ErrorCode.FORBIDDEN_ORDER_ACCESS;
import static com.securities.securities_server.global.exception.ErrorCode.ORDER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderCreateService orderCreateService;
    private final ExchangeOrderService exchangeOrderService;
    private final OrderResultService orderResultService;

    @Transactional
    public PlaceOrderResponse placeOrder(Long userId, PlaceOrderRequest request) {
        Order order = orderCreateService.createOrder(userId, request);

        ExchangeOrderRequest exchangeOrderRequest = toExchangeOrderRequest(order, request);
        ExchangeOrderResponse exchangeOrderResponse = exchangeOrderService.placeOrder(exchangeOrderRequest);

        orderResultService.handleExchangeOrderResponse(exchangeOrderResponse, order.getId());
        return new PlaceOrderResponse(order.getId(), exchangeOrderResponse.matchResult());
    }

    @Transactional
    public CancelOrderResponse cancelOrder(Long userId, Long orderId) {
        Order order = getOrder(orderId);
        validateOwner(userId, order);

        ExchangeCancelRequest exchangeCancelRequest = toExchangeCancelRequest(order);
        ExchangeOrderResponse exchangeOrderResponse = exchangeOrderService.cancelOrder(exchangeCancelRequest);

        orderResultService.handleExchangeOrderResponse(exchangeOrderResponse, orderId);
        return new CancelOrderResponse(orderId, exchangeOrderResponse.matchResult());
    }

    public UnfilledOrderResponse getUnfilledOrder(Long userId, Long stockId, OrderSide side, Pageable pageable) {
        Page<UnfilledOrder> unfilledOrders =
                orderRepository.findByUserIdAndStockIdAndSideAndUnfilledQuantityGreaterThan(userId, stockId, side, 0L, pageable)
                .map(UnfilledOrder::from);

        return new UnfilledOrderResponse(
                unfilledOrders.getTotalElements(),
                unfilledOrders.getContent()
        );
    }

    private ExchangeOrderRequest toExchangeOrderRequest(Order order, PlaceOrderRequest request) {
        return new ExchangeOrderRequest(
                order.getId(),
                order.getUser().getId(),
                request.stockId(),
                request.price(),
                request.quantity(),
                request.side(),
                order.getCreatedAt()
        );
    }

    private ExchangeCancelRequest toExchangeCancelRequest(Order order) {
        return new ExchangeCancelRequest(
                order.getId(),
                order.getStock().getId(),
                order.getSide(),
                order.getPrice()
        );
    }

    private void validateOwner(Long userId, Order order) {
        if (!order.getUser().getId().equals(userId)) {
            throw new CustomException(FORBIDDEN_ORDER_ACCESS);
        }
    }

    private Order getOrder(Long orderId){
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(ORDER_NOT_FOUND));
    }
}
