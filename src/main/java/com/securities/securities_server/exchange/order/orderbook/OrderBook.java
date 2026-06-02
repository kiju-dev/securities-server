package com.securities.securities_server.exchange.order.orderbook;

import com.securities.securities_server.exchange.order.ExchangeOrder;
import com.securities.securities_server.exchange.order.dto.response.ExchangeOrderResponse;
import com.securities.securities_server.exchange.order.dto.response.MatchedMakerOrder;
import com.securities.securities_server.securities.orderbook.controller.response.OrderBookResponse;
import com.securities.securities_server.securities.orderbook.controller.response.OrderSummary;
import com.securities.securities_server.securities.orderbook.controller.response.PriceLevel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.securities.securities_server.securities.order.entity.MatchResult.CANCELLED;
import static com.securities.securities_server.securities.order.entity.MatchResult.MATCHED;
import static com.securities.securities_server.securities.order.entity.MatchResult.UNMATCHED;
import static com.securities.securities_server.securities.order.entity.OrderSide.BUY;

public class OrderBook {

    private final Map<Long, Deque<ExchangeOrder>> buyBook = new HashMap<>();
    private final Map<Long, Deque<ExchangeOrder>> sellBook = new HashMap<>();
    private final Map<Long, ExchangeOrder> orderMap = new HashMap<>();

    public ExchangeOrderResponse place(ExchangeOrder order) {
        if (order.getSide() == BUY) {
            return match(order, sellBook);
        }
        return match(order, buyBook);
    }

    public ExchangeOrderResponse cancel(Long orderId) {
        ExchangeOrder order = orderMap.remove(orderId);

        Map<Long, Deque<ExchangeOrder>> orderBook;
        if (order.getSide() == BUY) {
            orderBook = buyBook;
        } else {
            orderBook = sellBook;
        }
        Deque<ExchangeOrder> orders = orderBook.get(order.getPrice());
        orders.remove(order);

        return new ExchangeOrderResponse(CANCELLED, null, List.of(), 0L, 0L);
    }

    public OrderBookResponse getOrderBook() {
        Map<Long, PriceLevel> buyPriceLevelMap = getPriceLevelMap(buyBook);
        Map<Long, PriceLevel> sellPriceLevelMap = getPriceLevelMap(sellBook);

        return new OrderBookResponse(buyPriceLevelMap, sellPriceLevelMap);
    }

    private Map<Long, PriceLevel> getPriceLevelMap(Map<Long, Deque<ExchangeOrder>> orderBook) {
        Map<Long, PriceLevel> priceLevelList = new HashMap<>();
        for (Map.Entry<Long, Deque<ExchangeOrder>> entry : orderBook.entrySet()) {
            long price = entry.getKey();
            Deque<ExchangeOrder> orders = entry.getValue();
            long totalQuantity = 0L;
            List<OrderSummary> orderSummaryList = new ArrayList<>();

            for (ExchangeOrder order : orders) {
                totalQuantity += order.getRemainingQuantity();
                orderSummaryList.add(new OrderSummary(order.getOrderId(), order.getRemainingQuantity(), order.getCreatedAt()));
            }
            priceLevelList.put(price, new PriceLevel(totalQuantity, orderSummaryList));
        }
        return priceLevelList;
    }

    private ExchangeOrderResponse match(ExchangeOrder takerOrder, Map<Long, Deque<ExchangeOrder>> makerOrderBook) {
        Deque<ExchangeOrder> makerOrders = makerOrderBook.get(takerOrder.getPrice());

        if (makerOrders == null || makerOrders.isEmpty()) {
            addToOrderBook(takerOrder);
            return new ExchangeOrderResponse(UNMATCHED, null, List.of(), 0L, 0L);
        }

        List<MatchedMakerOrder> makers = new ArrayList<>();
        long totalMatchedQuantity = 0L;

        while (takerOrder.getRemainingQuantity() > 0 && !makerOrders.isEmpty()) {
            ExchangeOrder makerOrder = makerOrders.getFirst();

            long matchedQuantity = Math.min(takerOrder.getRemainingQuantity(), makerOrder.getRemainingQuantity());
            takerOrder.decreaseQuantity(matchedQuantity);
            makerOrder.decreaseQuantity(matchedQuantity);

            makers.add(new MatchedMakerOrder(makerOrder.getOrderId(), matchedQuantity));
            totalMatchedQuantity += matchedQuantity;

            if (makerOrder.getRemainingQuantity() == 0) {
                makerOrders.pollFirst();
                orderMap.remove(makerOrder.getOrderId());
            }
        }

        if (makerOrders.isEmpty()) {
            makerOrderBook.remove(takerOrder.getPrice());
        }
        if (takerOrder.getRemainingQuantity() > 0) {
            addToOrderBook(takerOrder);
        }

        return new ExchangeOrderResponse(
                MATCHED,
                takerOrder.getOrderId(),
                makers,
                takerOrder.getPrice(),
                totalMatchedQuantity
        );
    }

    private void addToOrderBook(ExchangeOrder order) {
        Map<Long, Deque<ExchangeOrder>> orderBook;
        if (order.getSide() == BUY) {
            orderBook = buyBook;
        } else {
            orderBook = sellBook;
        }

        Deque<ExchangeOrder> orders = orderBook.get(order.getPrice());
        if (orders == null) {
            orders = new ArrayDeque<>();
            orderBook.put(order.getPrice(), orders);
        }

        orders.addLast(order);
        orderMap.put(order.getOrderId(), order);
    }

    public void close() {
        buyBook.clear();
        sellBook.clear();
        orderMap.clear();
    }
}
