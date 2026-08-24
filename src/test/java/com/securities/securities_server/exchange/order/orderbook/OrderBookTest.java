package com.securities.securities_server.exchange.order.orderbook;

import com.securities.securities_server.exchange.order.ExchangeOrder;
import com.securities.securities_server.exchange.order.dto.request.ExchangeOrderRequest;
import com.securities.securities_server.exchange.order.dto.response.ExchangeOrderResponse;
import com.securities.securities_server.global.exception.CustomException;
import com.securities.securities_server.global.common.OrderSide;
import com.securities.securities_server.exchange.order.dto.response.OrderBookResponse;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.securities.securities_server.global.exception.ErrorCode.ORDER_CANCEL_NOT_ALLOWED;
import static com.securities.securities_server.global.common.MatchResult.CANCELLED;
import static com.securities.securities_server.global.common.MatchResult.MATCHED;
import static com.securities.securities_server.global.common.MatchResult.UNMATCHED;
import static com.securities.securities_server.global.common.OrderSide.BUY;
import static com.securities.securities_server.global.common.OrderSide.SELL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OrderBookTest {

    private final OrderBook orderBook = new OrderBook();

    @Test
    void 매수_주문과_매칭되는_매도_주문이_없으면_매수_호가창에_등록된다() {
        // given
        ExchangeOrder buyOrder = order(1L, 10_000L, 10L, BUY);

        // when
        ExchangeOrderResponse response = orderBook.place(buyOrder);

        // then
        assertThat(response.matchResult()).isEqualTo(UNMATCHED);
        assertThat(response.takerOrderId()).isNull();
        assertThat(response.makers()).isEmpty();

        OrderBookResponse orderBookResponse = orderBook.getOrderBook();
        assertThat(orderBookResponse.buy()).containsKey(10_000L);
        assertThat(orderBookResponse.buy().get(10_000L).totalQuantity()).isEqualTo(10L);
        assertThat(orderBookResponse.buy().get(10_000L).orders())
                .extracting("orderId")
                .containsExactly(1L);
    }

    @Test
    void 매도_주문과_매칭되는_매수_주문이_없으면_매도_호가창에_등록된다() {
        // given
        ExchangeOrder sellOrder = order(1L, 10_000L, 10L, SELL);

        // when
        ExchangeOrderResponse response = orderBook.place(sellOrder);

        // then
        assertThat(response.matchResult()).isEqualTo(UNMATCHED);

        OrderBookResponse orderBookResponse = orderBook.getOrderBook();
        assertThat(orderBookResponse.sell()).containsKey(10_000L);
        assertThat(orderBookResponse.sell().get(10_000L).totalQuantity()).isEqualTo(10L);
    }

    @Test
    void 같은_가격의_반대_주문이_있으면_체결된다() {
        // given
        orderBook.place(order(1L, 10_000L, 10L, SELL));
        ExchangeOrder buyOrder = order(2L, 10_000L, 10L, BUY);

        // when
        ExchangeOrderResponse response = orderBook.place(buyOrder);

        // then
        assertThat(response.matchResult()).isEqualTo(MATCHED);
        assertThat(response.takerOrderId()).isEqualTo(2L);
        assertThat(response.price()).isEqualTo(10_000L);
        assertThat(response.totalMatchedQuantity()).isEqualTo(10L);
        assertThat(response.makers()).hasSize(1);
        assertThat(response.makers().get(0).orderId()).isEqualTo(1L);
        assertThat(response.makers().get(0).matchedQuantity()).isEqualTo(10L);

        OrderBookResponse orderBookResponse = orderBook.getOrderBook();
        assertThat(orderBookResponse.sell()).doesNotContainKey(10_000L);
        assertThat(orderBookResponse.buy()).doesNotContainKey(10_000L);
    }

    @Test
    void 먼저_들어온_주문부터_FIFO로_체결된다() {
        // given
        orderBook.place(order(1L, 10_000L, 5L, SELL));
        orderBook.place(order(2L, 10_000L, 5L, SELL));

        ExchangeOrder buyOrder = order(3L, 10_000L, 7L, BUY);

        // when
        ExchangeOrderResponse response = orderBook.place(buyOrder);

        // then
        assertThat(response.matchResult()).isEqualTo(MATCHED);
        assertThat(response.totalMatchedQuantity()).isEqualTo(7L);

        assertThat(response.makers()).hasSize(2);
        assertThat(response.makers().get(0).orderId()).isEqualTo(1L);
        assertThat(response.makers().get(0).matchedQuantity()).isEqualTo(5L);
        assertThat(response.makers().get(1).orderId()).isEqualTo(2L);
        assertThat(response.makers().get(1).matchedQuantity()).isEqualTo(2L);

        OrderBookResponse orderBookResponse = orderBook.getOrderBook();
        assertThat(orderBookResponse.sell().get(10_000L).totalQuantity()).isEqualTo(3L);
        assertThat(orderBookResponse.sell().get(10_000L).orders())
                .extracting("orderId")
                .containsExactly(2L);
    }

    @Test
    void taker_수량이_남으면_남은_수량이_호가창에_등록된다() {
        // given
        orderBook.place(order(1L, 10_000L, 5L, SELL));
        ExchangeOrder buyOrder = order(2L, 10_000L, 10L, BUY);

        // when
        ExchangeOrderResponse response = orderBook.place(buyOrder);

        // then
        assertThat(response.matchResult()).isEqualTo(MATCHED);
        assertThat(response.totalMatchedQuantity()).isEqualTo(5L);

        OrderBookResponse orderBookResponse = orderBook.getOrderBook();
        assertThat(orderBookResponse.sell()).doesNotContainKey(10_000L);
        assertThat(orderBookResponse.buy().get(10_000L).totalQuantity()).isEqualTo(5L);
        assertThat(orderBookResponse.buy().get(10_000L).orders())
                .extracting("orderId")
                .containsExactly(2L);
    }

    @Test
    void 주문을_취소하면_호가창에서_제거된다() {
        // given
        orderBook.place(order(1L, 10_000L, 10L, BUY));

        // when
        ExchangeOrderResponse response = orderBook.cancel(1L);

        // then
        assertThat(response.matchResult()).isEqualTo(CANCELLED);

        OrderBookResponse orderBookResponse = orderBook.getOrderBook();
        assertThat(orderBookResponse.buy()).doesNotContainKey(10_000L);
    }

    @Test
    void 이미_전량_체결된_주문을_취소하면_예외가_발생한다() {
        // given
        orderBook.place(order(1L, 10_000L, 10L, BUY));
        orderBook.place(order(2L, 10_000L, 10L, SELL));

        // when & then
        assertThatThrownBy(() -> orderBook.cancel(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ORDER_CANCEL_NOT_ALLOWED);
    }

    @Test
    void 폐장으로_호가창이_비워진_뒤_취소하면_예외가_발생한다() {
        // given
        orderBook.place(order(1L, 10_000L, 10L, BUY));
        orderBook.close();

        // when & then
        assertThatThrownBy(() -> orderBook.cancel(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ORDER_CANCEL_NOT_ALLOWED);
    }

    @Test
    void 이미_취소된_주문을_다시_취소하면_예외가_발생한다() {
        // given
        orderBook.place(order(1L, 10_000L, 10L, BUY));
        orderBook.cancel(1L);

        // when & then
        assertThatThrownBy(() -> orderBook.cancel(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ORDER_CANCEL_NOT_ALLOWED);
    }

    @Test
    void close를_호출하면_모든_호가창이_초기화된다() {
        // given
        orderBook.place(order(1L, 10_000L, 10L, BUY));
        orderBook.place(order(2L, 11_000L, 20L, SELL));

        // when
        orderBook.close();

        // then
        OrderBookResponse response = orderBook.getOrderBook();
        assertThat(response.buy()).isEmpty();
        assertThat(response.sell()).isEmpty();
    }

    private ExchangeOrder order(Long orderId, Long userId, long price, long quantity, OrderSide side) {
        ExchangeOrderRequest request = new ExchangeOrderRequest(
                orderId,
                userId,
                1L,
                price,
                quantity,
                side,
                LocalDateTime.of(2026, 5, 28, 10, 0)
        );

        return ExchangeOrder.from(request);
    }

    private ExchangeOrder order(Long orderId, long price, long quantity, OrderSide side) {
        ExchangeOrderRequest request = new ExchangeOrderRequest(
                orderId,
                orderId,
                1L,
                price,
                quantity,
                side,
                LocalDateTime.of(2026, 5, 28, 10, 0)
        );

        return ExchangeOrder.from(request);
    }

    @Test
    void 같은_사용자의_반대_주문과는_체결되지_않는다() {
        // given
        orderBook.place(order(1L, 100L, 10_000L, 10L, SELL));

        // when
        ExchangeOrderResponse response = orderBook.place(order(2L, 100L, 10_000L, 10L, BUY));

        // then
        assertThat(response.matchResult()).isEqualTo(UNMATCHED);
        assertThat(response.makers()).isEmpty();
        assertThat(response.totalMatchedQuantity()).isEqualTo(0L);

        OrderBookResponse orderBookResponse = orderBook.getOrderBook();
        assertThat(orderBookResponse.sell().get(10_000L).totalQuantity()).isEqualTo(10L);
        assertThat(orderBookResponse.buy().get(10_000L).totalQuantity()).isEqualTo(10L);
    }

    @Test
    void 자기_주문은_건너뛰고_다른_사용자의_주문과_체결된다() {
        // given
        orderBook.place(order(1L, 100L, 10_000L, 10L, SELL));
        orderBook.place(order(2L, 200L, 10_000L, 10L, SELL));

        // when
        ExchangeOrderResponse response = orderBook.place(order(3L, 100L, 10_000L, 10L, BUY));

        // then
        assertThat(response.matchResult()).isEqualTo(MATCHED);
        assertThat(response.makers()).hasSize(1);
        assertThat(response.makers().get(0).orderId()).isEqualTo(2L);
        assertThat(response.totalMatchedQuantity()).isEqualTo(10L);

        OrderBookResponse orderBookResponse = orderBook.getOrderBook();
        assertThat(orderBookResponse.sell().get(10_000L).orders())
                .extracting("orderId")
                .containsExactly(1L);
    }

}