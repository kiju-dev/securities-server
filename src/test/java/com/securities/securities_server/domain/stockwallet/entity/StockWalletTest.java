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
import static com.securities.securities_server.global.exception.ErrorCode.STOCK_WALLET_ALREADY_SUSPENDED;
import static com.securities.securities_server.global.exception.ErrorCode.STOCK_WALLET_NOT_SUSPENDED;
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

        @Nested
        class 조회_시 {

            @Test
            void 사용_가능한_수량은_보유_종목_수량에서_매도_주문으로_묶인_수량을_뺀_값이다() {
                // given
                StockWallet stockWallet = createStockWallet();
                stockWallet.credit(10L);

                // when
                long availableQuantity = stockWallet.getAvailableQuantity();

                // then
                assertThat(availableQuantity).isEqualTo(10L);
            }
        }

        @Nested
        class 정지_시 {

            @Test
            void 종목_계좌를_정지시킨다() {
                // given
                StockWallet stockWallet = createStockWallet();

                // when
                stockWallet.block();

                // then
                assertThat(stockWallet.isBlocked()).isTrue();
            }

            @Test
            void 이미_정지된_상태인_경우_STOCK_WALLET_ALREADY_SUSPENDED_예외가_발생한다() {
                // given
                StockWallet stockWallet = createStockWallet();
                stockWallet.block();

                // when & then
                assertThatThrownBy(stockWallet::block)
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(STOCK_WALLET_ALREADY_SUSPENDED);
            }
        }

        @Nested
        class 정지_해제_시 {

            @Test
            void 종목_계좌를_정지_해제시킨다() {
                // given
                StockWallet stockWallet = createStockWallet();
                stockWallet.block();

                // when
                stockWallet.unblock();

                // then
                assertThat(stockWallet.isBlocked()).isFalse();
            }

            @Test
            void 이미_정지_해제된_상태인_경우_STOCK_WALLET_NOT_SUSPENDED_예외가_발생한다() {
                // given
                StockWallet stockWallet = createStockWallet();

                // when & then
                assertThatThrownBy(stockWallet::unblock)
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(STOCK_WALLET_NOT_SUSPENDED);
            }
        }
    }

    private StockWallet createStockWallet() {
        User user = User.signUp("kiju", "kiju@gmail.com", "Passwer12!@");
        Stock stock = new Stock("삼성전자", "001123");
        return StockWallet.create(user, stock);
    }
}