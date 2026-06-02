package com.securities.securities_server.securities.cashwallet.service;

import com.securities.securities_server.securities.cashwallet.controller.request.DepositCashWalletRequest;
import com.securities.securities_server.securities.cashwallet.controller.request.WithdrawCashWalletRequest;
import com.securities.securities_server.securities.cashwallet.controller.response.CashWalletBalanceResponse;
import com.securities.securities_server.securities.cashwallet.controller.response.CreateCashWalletResponse;
import com.securities.securities_server.securities.cashwallet.controller.response.DepositCashWalletResponse;
import com.securities.securities_server.securities.cashwallet.controller.response.WithdrawCashWalletResponse;
import com.securities.securities_server.securities.cashwallet.entity.CashWallet;
import com.securities.securities_server.securities.cashwallet.entity.CashWalletTxType;
import com.securities.securities_server.securities.cashwallet.repository.CashWalletRepository;
import com.securities.securities_server.securities.cashwallet.service.dto.CashWalletHistoryCommand;
import com.securities.securities_server.securities.user.entity.User;
import com.securities.securities_server.securities.user.repository.UserRepository;
import com.securities.securities_server.global.exception.CustomException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.securities.securities_server.securities.cashwallet.entity.CashWalletTxType.WITHDRAW;
import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_ALREADY_EXISTS;
import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_SUSPENDED;
import static com.securities.securities_server.global.exception.ErrorCode.INSUFFICIENT_BALANCE;
import static com.securities.securities_server.global.exception.ErrorCode.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
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
            User user = createUser(1L);
            String accountNumber = "777123456781";
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(accountNumberGenerator.generateAccountNumber()).willReturn(accountNumber);

            // when
            CreateCashWalletResponse response = cashWalletService.createCashWallet(user.getId());

            // then
            assertThat(response.accountNumber()).isEqualTo(accountNumber);
            verify(cashWalletRepository).save(any(CashWallet.class));
        }

        @Test
        void 이미_개설된_현금_계좌가_존재하면_CASH_WALLET_ALREADY_EXISTS_예외가_발생한다() {
            // given
            User user = createUser(1L);
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(cashWalletRepository.existsByUser(user)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> cashWalletService.createCashWallet(user.getId()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(CASH_WALLET_ALREADY_EXISTS);
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
            User user = createUser(1L);
            DepositCashWalletRequest request = new DepositCashWalletRequest(10000L);
            CashWallet cashWallet = createCashWallet(user);
            given(cashWalletRepository.findByUserId(user.getId())).willReturn(Optional.of(cashWallet));

            // when
            DepositCashWalletResponse response = cashWalletService.depositCashWallet(user.getId(), request);

            // then
            assertThat(response.balanceAfter()).isEqualTo(10000L);
            CashWalletHistoryCommand command =
                    new CashWalletHistoryCommand(
                            cashWallet,
                            CashWalletTxType.DEPOSIT,
                            10000L,
                            10000L
                    );
            verify(cashWalletHistoryService).createCashWalletHistory(command);
        }

        @Test
        void 현금_계좌를_찾을_수_없으면_CASH_WALLET_NOT_FOUND_예외가_발생한다() {
            // given
            User user = createUser(1L);
            DepositCashWalletRequest request = new DepositCashWalletRequest(10000L);
            given(cashWalletRepository.findByUserId(user.getId())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cashWalletService.depositCashWallet(user.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(CASH_WALLET_NOT_FOUND);
            verify(cashWalletHistoryService, never()).createCashWalletHistory(any());
        }

        @Test
        void 현금_계좌가_정지_상태이면_CASH_WALLET_SUSPENDED_예외가_발생한다() {
            // given
            User user = createUser(1L);
            DepositCashWalletRequest request = new DepositCashWalletRequest(10000L);
            CashWallet cashWallet = createCashWallet(user);
            cashWallet.block();
            given(cashWalletRepository.findByUserId(user.getId())).willReturn(Optional.of(cashWallet));

            // when & then
            assertThatThrownBy(() -> cashWalletService.depositCashWallet(user.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(CASH_WALLET_SUSPENDED);
            verify(cashWalletHistoryService, never()).createCashWalletHistory(any());
        }
    }

    @Nested
    class 출금_시 {

        @Test
        void 현금_계좌의_잔액이_감소하고_현재_잔액을_반환한다() {
            // given
            User user = createUser(1L);
            WithdrawCashWalletRequest request = new WithdrawCashWalletRequest(10000L);
            CashWallet cashWallet = createCashWallet(user);
            cashWallet.deposit(30000L);
            given(cashWalletRepository.findByUserId(user.getId())).willReturn(Optional.of(cashWallet));

            // when
            WithdrawCashWalletResponse response = cashWalletService.withdrawCashWallet(user.getId(), request);

            // then
            assertThat(response.balanceAfter()).isEqualTo(20000L);
            CashWalletHistoryCommand command =
                    new CashWalletHistoryCommand(
                            cashWallet,
                            WITHDRAW,
                            10000L,
                            20000L
                    );
            verify(cashWalletHistoryService).createCashWalletHistory(command);
        }

        @Test
        void 현금_계좌를_찾을_수_없으면_CASH_WALLET_NOT_FOUND_예외가_발생한다() {
            // given
            User user = createUser(1L);
            WithdrawCashWalletRequest request = new WithdrawCashWalletRequest(10000L);
            given(cashWalletRepository.findByUserId(user.getId())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cashWalletService.withdrawCashWallet(user.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(CASH_WALLET_NOT_FOUND);
            verify(cashWalletHistoryService, never()).createCashWalletHistory(any());
        }

        @Test
        void 현금_계좌가_정지_상태이면_CASH_WALLET_SUSPENDED_예외가_발생한다() {
            // given
            User user = createUser(1L);
            WithdrawCashWalletRequest request = new WithdrawCashWalletRequest(10000L);
            CashWallet cashWallet = createCashWallet(user);
            cashWallet.block();
            given(cashWalletRepository.findByUserId(user.getId())).willReturn(Optional.of(cashWallet));

            // when & then
            assertThatThrownBy(() -> cashWalletService.withdrawCashWallet(user.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(CASH_WALLET_SUSPENDED);
            verify(cashWalletHistoryService, never()).createCashWalletHistory(any());
        }

        @Test
        void 출금_금액이_사용_가능한_잔액보다_크면_INSUFFICIENT_BALANCE_예외가_발생한다() {
            // given
            User user = createUser(1L);
            WithdrawCashWalletRequest request = new WithdrawCashWalletRequest(10000L);
            CashWallet cashWallet = createCashWallet(user);
            given(cashWalletRepository.findByUserId(user.getId())).willReturn(Optional.of(cashWallet));

            // when & then
            assertThatThrownBy(() -> cashWalletService.withdrawCashWallet(user.getId(), request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(INSUFFICIENT_BALANCE);
            verify(cashWalletHistoryService, never()).createCashWalletHistory(any());
        }
    }

    @Nested
    class 잔액_조회_시 {

        @Test
        void 현금_계좌의_잔액과_매수주문으로_묶인_금액과_사용가능한_잔액을_반환한다() {
            // given
            User user = createUser(1L);
            CashWallet cashWallet = createCashWallet(user);
            cashWallet.deposit(10000L);
            given(cashWalletRepository.findByUserId(user.getId())).willReturn(Optional.of(cashWallet));

            // when
            CashWalletBalanceResponse response = cashWalletService.getBalance(user.getId());

            // then
            assertThat(response.balance()).isEqualTo(10000L);
            assertThat(response.lockedAmount()).isEqualTo(0L);
            assertThat(response.availableAmount()).isEqualTo(10000L);
        }

        @Test
        void 현금_계좌를_찾을_수_없으면_CASH_WALLET_NOT_FOUND_예외가_발생한다() {
            // given
            User user = createUser(1L);
            given(cashWalletRepository.findByUserId(user.getId())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cashWalletService.getBalance(user.getId()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(CASH_WALLET_NOT_FOUND);
        }
    }

    @Nested
    class 내역_조회_시 {

        @Test
        void 현금_계좌를_찾을_수_없으면_CASH_WALLET_NOT_FOUND_예외가_발생한다() {
            // given
            User user = createUser(1L);
            Pageable pageable = PageRequest.of(0, 10);
            given(cashWalletRepository.findByUserId(user.getId())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cashWalletService.getHistories(user.getId(), pageable))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(CASH_WALLET_NOT_FOUND);
        }
    }

    @Nested
    class 계좌_정지_시 {

        @Test
        void 현금_계좌를_정지한다() {
            // given
            Long userId = 1L;
            Long cashWalletId = 1L;
            User user = createUser(userId);
            CashWallet cashWallet = createCashWallet(user);

            given(cashWalletRepository.findById(cashWalletId)).willReturn(Optional.of(cashWallet));

            // when
            cashWalletService.blockCashWallet(cashWalletId);

            // then
            assertThat(cashWallet.isBlocked()).isTrue();
        }

        @Test
        void 현금_계좌를_찾을_수_없으면_CASH_WALLET_NOT_FOUND_예외가_발생한다() {
            // given
            Long cashWalletId = 1L;
            given(cashWalletRepository.findById(cashWalletId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cashWalletService.blockCashWallet(cashWalletId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(CASH_WALLET_NOT_FOUND);
        }
    }

    @Nested
    class 계좌_정지_해제_시 {

        @Test
        void 현금_계좌_정지를_해제한다() {
            // given
            Long userId = 1L;
            Long cashWalletId = 1L;
            User user = createUser(userId);
            CashWallet cashWallet = createCashWallet(user);
            cashWallet.block();

            given(cashWalletRepository.findById(cashWalletId)).willReturn(Optional.of(cashWallet));

            // when
            cashWalletService.unblockCashWallet(cashWalletId);

            // then
            assertThat(cashWallet.isBlocked()).isFalse();
        }

        @Test
        void 현금_계좌를_찾을_수_없으면_CASH_WALLET_NOT_FOUND_예외가_발생한다() {
            // given
            Long cashWalletId = 1L;
            given(cashWalletRepository.findById(cashWalletId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cashWalletService.unblockCashWallet(cashWalletId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(CASH_WALLET_NOT_FOUND);
        }
    }

    private User createUser(Long userId) {
        User user = User.signUp("kiju", "kiju@gmail.com", "Passwo12!@");
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private CashWallet createCashWallet(User user) {
        return CashWallet.create(user, "777123456781");
    }
}