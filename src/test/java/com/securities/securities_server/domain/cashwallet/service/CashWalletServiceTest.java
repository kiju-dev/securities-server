package com.securities.securities_server.domain.cashwallet.service;

import com.securities.securities_server.domain.cashwallet.controller.request.DepositCashWalletRequest;
import com.securities.securities_server.domain.cashwallet.controller.response.CreateCashWalletResponse;
import com.securities.securities_server.domain.cashwallet.controller.response.DepositCashWalletResponse;
import com.securities.securities_server.domain.cashwallet.entity.CashWallet;
import com.securities.securities_server.domain.cashwallet.repository.CashWalletRepository;
import com.securities.securities_server.domain.user.entity.User;
import com.securities.securities_server.domain.user.repository.UserRepository;
import com.securities.securities_server.global.exception.CustomException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_SUSPENDED;
import static com.securities.securities_server.global.exception.ErrorCode.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CashWalletServiceTest {

    @Mock
    CashWalletHistoryService cashWalletHistoryService;
    @Mock
    CashWalletRepository cashWalletRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    AccountNumberGenerator accountNumberGenerator;

    @InjectMocks
    CashWalletService cashWalletService;

    @Nested
    class 개설_시 {

        @Test
        void 현금_계좌를_개설하고_계좌번호를_반환한다() {
            // given
            User user = createUser();
            String accountNumber = "777123456781";
            given(userRepository.findById(any())).willReturn(Optional.of(user));
            given(accountNumberGenerator.generateAccountNumber()).willReturn(accountNumber);

            // when
            CreateCashWalletResponse response = cashWalletService.createCashWallet(user.getId());

            // then
            assertThat(response.accountNumber()).isEqualTo(accountNumber);
            verify(cashWalletRepository).save(any(CashWallet.class));
        }

        @Test
        void User를_찾을_수_없으면_USER_NOT_FOUND_예외가_발생한다() {
            // given
            Long userId = 1L;
            given(userRepository.findById(any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cashWalletService.createCashWallet(userId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(USER_NOT_FOUND);
        }
    }

    @Nested
    class 입금_시 {

        @Test
        void 현금_계좌의_잔액이_증가하고_현재_잔액을_반환한다() {
            // given
            Long userId = 1L;
            DepositCashWalletRequest request = new DepositCashWalletRequest(10000L);
            CashWallet cashWallet = createCashWallet();
            given(userRepository.findById(userId)).willReturn(Optional.of(cashWallet.getUser()));
            given(cashWalletRepository.findByUser(cashWallet.getUser())).willReturn(Optional.of(cashWallet));

            // when
            DepositCashWalletResponse response = cashWalletService.depositCashWallet(userId, request);

            // then
            assertThat(response.balanceAfter()).isEqualTo(cashWallet.getBalance());
        }

        @Test
        void User를_찾을_수_없으면_USER_NOT_FOUND_예외가_발생한다() {
            // given
            Long userId = 1L;
            DepositCashWalletRequest request = new DepositCashWalletRequest(10000L);
            given(userRepository.findById(any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cashWalletService.depositCashWallet(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(USER_NOT_FOUND);
        }

        @Test
        void 현금_계좌를_찾을_수_없으면_CASH_WALLET_NOT_FOUND_예외가_발생한다() {
            // given
            Long userId = 1L;
            User user = createUser();
            DepositCashWalletRequest request = new DepositCashWalletRequest(10000L);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(cashWalletRepository.findByUser(user)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cashWalletService.depositCashWallet(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(CASH_WALLET_NOT_FOUND);
        }

        @Test
        void 현금_계좌가_정지_상태이면_CASH_WALLET_SUSPENDED_예외가_발생한다() {
            // given
            Long userId = 1L;
            DepositCashWalletRequest request = new DepositCashWalletRequest(10000L);
            CashWallet cashWallet = createCashWallet();
            cashWallet.block();
            given(userRepository.findById(userId)).willReturn(Optional.of(cashWallet.getUser()));
            given(cashWalletRepository.findByUser(cashWallet.getUser())).willReturn(Optional.of(cashWallet));

            // when & then
            assertThatThrownBy(() -> cashWalletService.depositCashWallet(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(CASH_WALLET_SUSPENDED);
        }
    }

    private User createUser() {
        return User.signUp("kiju", "kiju@gmail.com", "Passwo12!@");
    }

    private CashWallet createCashWallet() {
        User user = createUser();
        String accountNumber = "777123456781";
        return CashWallet.create(user, accountNumber);
    }
}