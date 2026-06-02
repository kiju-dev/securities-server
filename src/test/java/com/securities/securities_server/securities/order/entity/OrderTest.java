package com.securities.securities_server.securities.order.entity;

import com.securities.securities_server.securities.stock.entity.Stock;
import com.securities.securities_server.securities.user.entity.User;
import com.securities.securities_server.global.exception.CustomException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.securities.securities_server.securities.order.entity.OrderSide.BUY;
import static com.securities.securities_server.global.exception.ErrorCode.INVALID_MATCH_QUANTITY;
import static com.securities.securities_server.global.exception.ErrorCode.ORDER_CANCEL_NOT_ALLOWED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class OrderTest {

    @Nested
    class 주문_체결_시 {

        @Test
        void 체결_된_수량만큼_unfilledQuantity가_감소한다() {
            // given
            User user = User.signUp("kiju", "kiju@gmail.com", "Pass123!@");
            Stock stock = new Stock("삼성전자", "001123");
            Order order = Order.create(user, stock, BUY, 20000L, 10L);

            // when
            order.fill(5L);

            // then
            assertThat(order.getUnfilledQuantity()).isEqualTo(5L);
        }

        @ParameterizedTest
        @ValueSource(longs = {1000L, 100L, 11L})
        void 체결_된_수량이_미체결_수량보다_크면_INVALID_MATCH_QUANTITY_예외가_발생한다(long quantity) {
            // given
            User user = User.signUp("kiju", "kiju@gmail.com", "Pass123!@");
            Stock stock = new Stock("삼성전자", "001123");
            Order order = Order.create(user, stock, BUY, 20000L, 10L);

            // when & then
            assertThatThrownBy(() -> order.fill(quantity))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(INVALID_MATCH_QUANTITY);
        }
    }

    @Nested
    class 주문_취소_시 {

        @Test
        void unfilledQuantity_수량을_취소_수량으로_변경한다() {
            // given
            User user = User.signUp("kiju", "kiju@gmail.com", "Pass123!@");
            Stock stock = new Stock("삼성전자", "001123");
            Order order = Order.create(user, stock, BUY, 20000L, 10L);

            // when
            order.cancelRemainingQuantity();

            // then
            assertThat(order.getUnfilledQuantity()).isEqualTo(0L);
            assertThat(order.getCanceledQuantity()).isEqualTo(10L);
        }

        @Test
        void unfilledQuantity_수량이_0일경우_ORDER_CANCEL_NOT_ALLOWED_예외가_발생한다() {
            // given
            User user = User.signUp("kiju", "kiju@gmail.com", "Pass123!@");
            Stock stock = new Stock("삼성전자", "001123");
            Order order = Order.create(user, stock, BUY, 20000L, 10L);
            order.fill(10L);

            // when & then
            assertThatThrownBy(order::cancelRemainingQuantity)
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ORDER_CANCEL_NOT_ALLOWED);
        }
    }

}