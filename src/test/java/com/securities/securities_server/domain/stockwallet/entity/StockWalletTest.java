package com.securities.securities_server.domain.stockwallet.entity;

import com.securities.securities_server.domain.stock.entity.Stock;
import com.securities.securities_server.domain.user.entity.User;
import com.securities.securities_server.global.exception.CustomException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.securities.securities_server.global.exception.ErrorCode.INVALID_QUANTITY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StockWalletTest {

    @Nested
    class 종목_계좌는 {

        @Nested
        class 개설_시 {

            @Test
            void 보유_종목_수량은_0이다() {
                // given
                StockWallet stockWallet = createStockWallet();

                // when
                long holdingQuantity = stockWallet.getHoldingQuantity();

                // then
                assertThat(holdingQuantity).isEqualTo(0L);
            }

            @Test
            void 매도_수량은_0이다() {
                // given
                StockWallet stockWallet = createStockWallet();

                // when
                long lockedQuantity = stockWallet.getLockedQuantity();

                // then
                assertThat(lockedQuantity).isEqualTo(0L);
            }

            @Test
            void 정지_상태가_아니다() {
                // given
                StockWallet stockWallet = createStockWallet();

                // when
                boolean blocked = stockWallet.isBlocked();

                // then
                assertThat(blocked).isFalse();
            }
        }

        @Nested
        class 입고_시 {

            @Test
            void 입고_수량만큼_보유_수량이_증가한다() {
                // given
                StockWallet stockWallet = createStockWallet();

                // when
                stockWallet.credit(10L);

                // then
                assertThat(stockWallet.getHoldingQuantity()).isEqualTo(10L);
            }

            @ParameterizedTest
            @ValueSource(longs = {-100L, -10L, 0})
            void 입고_수량이_0이거나_음수인_경우에_INVALID_QUANTITY_예외가_발생한다(long quantity) {
                // given
                StockWallet stockWallet = createStockWallet();

                // when
                assertThatThrownBy(() -> stockWallet.credit(quantity))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(INVALID_QUANTITY);
            }
        }
    }

    private StockWallet createStockWallet() {
        User user = User.signUp("kiju", "kiju@gmail.com", "Passwer12!@");
        Stock stock = new Stock("삼성전자", "001123");
        return StockWallet.create(user, stock);
    }
}