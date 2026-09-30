package com.securities.securities_server.exchange.order.orderbook;

import com.securities.securities_server.exchange.order.ExchangeOrder;
import com.securities.securities_server.exchange.order.dto.request.ExchangeOrderRequest;
import com.securities.securities_server.exchange.order.dto.response.ExchangeOrderResponse;
import com.securities.securities_server.exchange.order.dto.response.MatchedMakerOrder;
import com.securities.securities_server.exchange.order.dto.response.OrderBookResponse;
import com.securities.securities_server.exchange.order.dto.response.PriceLevel;
import com.securities.securities_server.global.common.OrderSide;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.securities.securities_server.global.common.MatchResult.MATCHED;
import static com.securities.securities_server.global.common.OrderSide.BUY;
import static com.securities.securities_server.global.common.OrderSide.SELL;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OrderBookConcurrencyTest {

    private static final long PRICE = 10_000L;

    @Test
    void 동시에_매수_주문이_들어오면_호가창에_등록된_주문이_유실된다() throws Exception {
        // given
        OrderBook orderBook = new OrderBook();
        int orderCount = 100;

        // when
        runConcurrently(orderCount, i ->
                orderBook.place(order(i, i, PRICE, 10L, BUY))
        );

        // then
        OrderBookResponse response = orderBook.getOrderBook();
        PriceLevel priceLevel = response.buy().get(PRICE);

        assertThat(priceLevel).isNotNull();
        assertThat(priceLevel.orders())
                .as("동시 등록된 매수 주문이 유실되지 않아야 한다")
                .hasSize(orderCount);
        assertThat(priceLevel.totalQuantity())
                .as("호가창 총 잔량은 100건 * 10주 = 1000주 여야 한다")
                .isEqualTo(orderCount * 10L);
    }

    @Test
    void 하나의_매도_물량에_매수가_동시에_몰리면_보유량보다_많이_체결된다() throws Exception {
        // given
        OrderBook orderBook = new OrderBook();
        long sellQuantity = 10L;
        orderBook.place(order(0L, 0L, PRICE, sellQuantity, SELL));

        // when
        int buyerCount = 50;
        AtomicLong totalMatched = new AtomicLong();
        runConcurrently(buyerCount, i -> {
            ExchangeOrderResponse response = orderBook.place(order(i, i, PRICE, 1L, BUY));
            if (response.matchResult() == MATCHED) {
                long matched = response.makers().stream()
                        .mapToLong(MatchedMakerOrder::matchedQuantity)
                        .sum();
                totalMatched.addAndGet(matched);
            }
        });

        // then
        assertThat(totalMatched.get())
                .as("총 체결 수량은 매도 물량 %d주를 초과할 수 없다", sellQuantity)
                .isLessThanOrEqualTo(sellQuantity);
    }

    @Test
    void 체결과_동시에_호가창을_조회하면_예외가_발생한다() throws Exception {
        // given
        OrderBook orderBook = new OrderBook();
        for (int i = 0; i < 200; i++) {
            orderBook.place(order((long) i, (long) i, PRICE + i, 10L, BUY));
        }

        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
        int taskCount = 40;

        // when
        runConcurrently(taskCount, errors, i -> {
            if (i % 2 == 0) {
                orderBook.place(order(1_000L + i, i, PRICE + i, 10L, BUY));
                return;
            }
            orderBook.getOrderBook();
        });

        // then
        assertThat(errors)
                .as("호가창 조회는 동시 주문과 무관하게 안전해야 한다. 발생한 예외: %s", errors)
                .isEmpty();
    }

    private void runConcurrently(int taskCount, ThrowingIntConsumer task) throws Exception {
        ConcurrentLinkedQueue<Throwable> errors = new ConcurrentLinkedQueue<>();
        runConcurrently(taskCount, errors, task);
        if (!errors.isEmpty()) {
            Throwable first = errors.peek();
            throw new AssertionError("동시 실행 중 " + errors.size() + "건의 예외 발생. 첫 예외: " + first, first);
        }
    }

    private void runConcurrently(int taskCount,
            ConcurrentLinkedQueue<Throwable> errors,
            ThrowingIntConsumer task
    ) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(taskCount);
        CountDownLatch ready = new CountDownLatch(taskCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(taskCount);

        try {
            for (int i = 0; i < taskCount; i++) {
                long index = i;
                executor.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();                        task.accept(index);
                    } catch (Throwable t) {
                        errors.add(t);
                    } finally {
                        done.countDown();
                    }
                });
            }

            ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            boolean finished = done.await(10, TimeUnit.SECONDS);
            assertThat(finished)
                    .as("모든 주문 처리가 10초 안에 끝나야 한다 (HashMap 리해싱 무한 루프 의심)")
                    .isTrue();
        } finally {
            executor.shutdownNow();
        }
    }

    private ExchangeOrder order(long orderId, long userId, long price, long quantity, OrderSide side) {
        return ExchangeOrder.from(new ExchangeOrderRequest(orderId,
                userId,
                1L,
                price,
                quantity,
                side,
                LocalDateTime.now()
        ));
    }

    @FunctionalInterface
    private interface ThrowingIntConsumer {
        void accept(long index) throws Exception;
    }
}
