package com.securities.securities_server.domain.cashwallet.entity;

import com.securities.securities_server.domain.user.entity.User;
import com.securities.securities_server.global.exception.CustomException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_ALREADY_SUSPENDED;
import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_NOT_SUSPENDED;
import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_SUSPENDED;
import static com.securities.securities_server.global.exception.ErrorCode.INSUFFICIENT_BALANCE;
import static com.securities.securities_server.global.exception.ErrorCode.INVALID_AMOUNT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CashWalletTest {

    @Nested
    class 현금_계좌는 {

        @Nested
        class 개설_시 {

            @Test
            void 잔액은_0원이다() {
                // given
                CashWallet cashWallet = createCashWallet();

                // when
                long balance = cashWallet.getBalance();

                // then
                assertThat(balance).isEqualTo(0L);
            }

            @Test
            void 매수_주문으로_묶인_금액은_0원이다() {
                // given
                CashWallet cashWallet = createCashWallet();

                // when
                long lockedAmount = cashWallet.getLockedAmount();

                // then
                assertThat(lockedAmount).isEqualTo(0L);
            }

            @Test
            void 정지_상태가_아니다() {
                // given
                CashWallet cashWallet = createCashWallet();

                // when
                boolean blocked = cashWallet.isBlocked();

                // then
                assertThat(blocked).isFalse();
            }
        }

        @Nested
        class 입금_시 {

            @Test
            void 입금_금액만큼_잔액이_증가한다() {
                // given
                CashWallet cashWallet = createCashWallet();
                long balanceBefore = cashWallet.getBalance();
                long amount = 20000L;

                // when
                cashWallet.deposit(amount);

                // then
                assertThat(cashWallet.getBalance()).isEqualTo(balanceBefore + amount);
            }

            @ParameterizedTest
            @ValueSource(longs = {-10000L, -400L, -1L, 0L})
            void 입금_금액이_0이거나_음수인_경우_예외가_발생한다(long amount) {
                // given
                CashWallet cashWallet = createCashWallet();

                // when & then
                assertThatThrownBy(() -> cashWallet.deposit(amount))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(INVALID_AMOUNT);
            }
        }

        @Nested
        class 출금_시 {

            @Test
            void 출금_금액만큼_잔액이_감소한다() {
                // given
                CashWallet cashWallet = createCashWallet();
                cashWallet.deposit(30000L);

                // when
                cashWallet.withdraw(20000L);

                // then
                assertThat(cashWallet.getBalance()).isEqualTo(10000L);
            }

            @ParameterizedTest
            @ValueSource(longs = {-10000L, -400L, -1L, 0L})
            void 출금_금액이_0이거나_음수인_경우_예외가_발생한다(long amount) {
                // given
                CashWallet cashWallet = createCashWallet();

                // when & then
                assertThatThrownBy(() -> cashWallet.withdraw(amount))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(INVALID_AMOUNT);
            }

            @ParameterizedTest
            @ValueSource(longs = {20001L, 30000L, 100000L})
            void 출금_금액이_사용가능한_잔액보다_크면_예외가_발생한다(long amount) {
                // given
                CashWallet cashWallet = createCashWallet();
                cashWallet.deposit(20000L);

                // when & then
                assertThatThrownBy(() -> cashWallet.withdraw(amount))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(INSUFFICIENT_BALANCE);
            }
        }

        @Nested
        class 정지_시 {

            @Test
            void 현금_계좌가_정지된다() {
                // given
                CashWallet cashWallet = createCashWallet();

                // when
                cashWallet.block();

                // then
                assertThat(cashWallet.isBlocked()).isTrue();
            }

            @Test
            void 이미_정지된_경우_CASH_WALLET_ALREADY_SUSPENDED_예외가_발생한다() {
                // given
                CashWallet cashWallet = createCashWallet();
                cashWallet.block();

                // when & then
                assertThatThrownBy(cashWallet::block)
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(CASH_WALLET_ALREADY_SUSPENDED);
                assertThat(cashWallet.isBlocked()).isTrue();
            }

            @Test
            void 입금을_시도하면_CASH_WALLET_SUSPENDED_예외가_발생한다() {
                // given
                CashWallet cashWallet = createCashWallet();
                cashWallet.block();

                // when & then
                assertThatThrownBy(() -> cashWallet.deposit(1000L))
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(CASH_WALLET_SUSPENDED);
            }
        }

        @Nested
        class 정지_해제_시 {

            @Test
            void 현금_계좌가_정지_해제된다() {
                // given
                CashWallet cashWallet = createCashWallet();
                cashWallet.block();
                boolean checkBlocked = cashWallet.isBlocked();

                // when
                cashWallet.unblock();

                // then
                assertThat(cashWallet.isBlocked()).isFalse();
                assertThat(checkBlocked).isTrue();
            }

            @Test
            void 이미_해제된_경우_CASH_WALLET_NOT_SUSPENDED_예외가_발생한다() {
                // given
                CashWallet cashWallet = createCashWallet();

                // when & then
                assertThatThrownBy(cashWallet::unblock)
                        .isInstanceOf(CustomException.class)
                        .extracting("errorCode")
                        .isEqualTo(CASH_WALLET_NOT_SUSPENDED);
                assertThat(cashWallet.isBlocked()).isFalse();
            }
        }
    }

    private CashWallet createCashWallet() {
        User user = createUser();
        String accountNumber = "777123456781";
        return CashWallet.create(user, accountNumber);
    }

    private User createUser() {
        return User.signUp("kiju", "kiju@gmail.com", "Paswer12!@");
    }
}