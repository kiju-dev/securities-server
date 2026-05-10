package com.securities.securities_server.domain.stockwallet.entity;

import com.securities.securities_server.domain.stock.entity.Stock;
import com.securities.securities_server.domain.user.entity.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    }

    private StockWallet createStockWallet() {
        User user = User.signUp("kiju", "kiju@gmail.com", "Passwer12!@");
        Stock stock = new Stock("삼성전자", "001123");
        return StockWallet.create(user, stock);
    }
}