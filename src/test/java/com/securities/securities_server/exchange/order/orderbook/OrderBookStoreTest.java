package com.securities.securities_server.exchange.order.orderbook;

import com.securities.securities_server.exchange.order.ExchangeOrder;
import com.securities.securities_server.exchange.order.dto.request.ExchangeOrderRequest;
import com.securities.securities_server.global.common.OrderSide;
import com.securities.securities_server.exchange.order.dto.response.OrderBookResponse;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.securities.securities_server.global.common.OrderSide.BUY;
import static com.securities.securities_server.global.common.OrderSide.SELL;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OrderBookStoreTest {

    private final OrderBookStore orderBookStore = new OrderBookStore();

    @Test
    void 종목_ID로_OrderBook을_조회한다() {
        // given
        Long stockId = 1L;

        // when
        OrderBook orderBook = orderBookStore.getOrderBook(stockId);

        // then
        assertThat(orderBook).isNotNull();
    }

    @Test
    void 같은_종목_ID로_조회하면_같은_OrderBook을_반환한다() {
        // given
        Long stockId = 1L;

        // when
        OrderBook firstOrderBook = orderBookStore.getOrderBook(stockId);
        OrderBook secondOrderBook = orderBookStore.getOrderBook(stockId);

        // then
        assertThat(secondOrderBook).isSameAs(firstOrderBook);
    }

    @Test
    void 다른_종목_ID로_조회하면_다른_OrderBook을_반환한다() {
        // given
        Long firstStockId = 1L;
        Long secondStockId = 2L;

        // when
        OrderBook firstOrderBook = orderBookStore.getOrderBook(firstStockId);
        OrderBook secondOrderBook = orderBookStore.getOrderBook(secondStockId);

        // then
        assertThat(secondOrderBook).isNotSameAs(firstOrderBook);
    }

    @Test
    void closeAll을_호출하면_모든_OrderBook이_초기화된다() {
        // given
        OrderBook firstOrderBook = orderBookStore.getOrderBook(1L);
        OrderBook secondOrderBook = orderBookStore.getOrderBook(2L);

        firstOrderBook.place(order(1L, 1L, 10_000L, 10L, BUY));
        secondOrderBook.place(order(2L, 2L, 20_000L, 20L, SELL));

        // when
        orderBookStore.closeAll();

        // then
        OrderBookResponse firstResponse = firstOrderBook.getOrderBook();
        OrderBookResponse secondResponse = secondOrderBook.getOrderBook();

        assertThat(firstResponse.buy()).isEmpty();
        assertThat(firstResponse.sell()).isEmpty();

        assertThat(secondResponse.buy()).isEmpty();
        assertThat(secondResponse.sell()).isEmpty();
    }

    @Test
    void closeAll_이후에도_같은_종목_ID는_같은_OrderBook을_반환한다() {
        // given
        Long stockId = 1L;
        OrderBook beforeClose = orderBookStore.getOrderBook(stockId);

        beforeClose.place(order(1L, stockId, 10_000L, 10L, BUY));

        // when
        orderBookStore.closeAll();
        OrderBook afterClose = orderBookStore.getOrderBook(stockId);

        // then
        assertThat(afterClose).isSameAs(beforeClose);

        OrderBookResponse response = afterClose.getOrderBook();
        assertThat(response.buy()).isEmpty();
        assertThat(response.sell()).isEmpty();
    }

    private ExchangeOrder order(
            Long orderId,
            Long stockId,
            long price,
            long quantity,
            OrderSide side
    ) {
        ExchangeOrderRequest request = new ExchangeOrderRequest(
                orderId,
                orderId,
                stockId,
                price,
                quantity,
                side,
                LocalDateTime.of(2026, 5, 28, 10, 0)
        );

        return ExchangeOrder.from(request);
    }
}