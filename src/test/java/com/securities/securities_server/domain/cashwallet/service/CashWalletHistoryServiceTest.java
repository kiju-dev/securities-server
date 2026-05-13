package com.securities.securities_server.domain.cashwallet.service;

import com.securities.securities_server.domain.cashwallet.controller.response.CashWalletHistoriesResponse;
import com.securities.securities_server.domain.cashwallet.entity.CashWallet;
import com.securities.securities_server.domain.cashwallet.entity.CashWalletHistory;
import com.securities.securities_server.domain.cashwallet.repository.CashWalletHistoryRepository;
import com.securities.securities_server.domain.cashwallet.service.dto.CashWalletHistoryCommand;
import com.securities.securities_server.domain.user.entity.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.securities.securities_server.domain.cashwallet.entity.CashWalletTxType.DEPOSIT;
import static com.securities.securities_server.domain.cashwallet.entity.CashWalletTxType.WITHDRAW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CashWalletHistoryServiceTest {

    @Mock
    CashWalletHistoryRepository cashWalletHistoryRepository;

    @InjectMocks
    CashWalletHistoryService cashWalletHistoryService;

    @Test
    void 현금_계좌에_입금_내역을_저장한다() {
        // given
        CashWallet cashWallet = createCashWallet();
        CashWalletHistoryCommand command =
                new CashWalletHistoryCommand(
                        cashWallet,
                        DEPOSIT,
                        10000L,
                        cashWallet.getBalance()
                );

        // when
        cashWalletHistoryService.createCashWalletHistory(command);

        // then
        ArgumentCaptor<CashWalletHistory> captor =
                ArgumentCaptor.forClass(CashWalletHistory.class);
        verify(cashWalletHistoryRepository).save(captor.capture());
        CashWalletHistory savedHistory = captor.getValue();

        assertThat(savedHistory.getCashWallet()).isEqualTo(cashWallet);
        assertThat(savedHistory.getTxType()).isEqualTo(DEPOSIT);
        assertThat(savedHistory.getTxAmount()).isEqualTo(10000L);
        assertThat(savedHistory.getBalanceAfter()).isEqualTo(cashWallet.getBalance());
    }

    @Test
    void 현금_계좌에_출금_내역을_저장한다() {
        // given
        CashWallet cashWallet = createCashWallet();
        CashWalletHistoryCommand command =
                new CashWalletHistoryCommand(
                        cashWallet,
                        WITHDRAW,
                        20000L,
                        10000L
                );

        // when
        cashWalletHistoryService.createCashWalletHistory(command);

        // then
        ArgumentCaptor<CashWalletHistory> captor =
                ArgumentCaptor.forClass(CashWalletHistory.class);
        verify(cashWalletHistoryRepository).save(captor.capture());
        CashWalletHistory savedHistory = captor.getValue();

        assertThat(savedHistory.getCashWallet()).isEqualTo(cashWallet);
        assertThat(savedHistory.getTxType()).isEqualTo(WITHDRAW);
        assertThat(savedHistory.getTxAmount()).isEqualTo(20000L);
        assertThat(savedHistory.getBalanceAfter()).isEqualTo(10000L);
    }

    @Test
    void 현금_계좌_내역을_페이지로_조회한다() {
        // given
        CashWallet cashWallet = createCashWallet();
        CashWalletHistory history1 =
                CashWalletHistory.create(cashWallet, DEPOSIT, 10000L, 20000L);
        CashWalletHistory history2 =
                CashWalletHistory.create(cashWallet, DEPOSIT, 20000L, 40000L);

        Pageable pageable = PageRequest.of(0, 10);

        PageImpl<CashWalletHistory> page = new PageImpl<>(List.of(history1, history2), pageable, 2);
        given(cashWalletHistoryRepository.findByCashWallet(cashWallet, pageable)).willReturn(page);

        // when
        CashWalletHistoriesResponse response = cashWalletHistoryService.getHistories(cashWallet, pageable);

        // then
        assertThat(response.totalElements()).isEqualTo(2);
        assertThat(response.cashWalletHistories()).hasSize(2);
        assertThat(response.cashWalletHistories().get(0).txAmount()).isEqualTo(10000L);
        assertThat(response.cashWalletHistories().get(1).txAmount()).isEqualTo(20000L);
    }

    private CashWallet createCashWallet() {
        User user = User.signUp("kiju", "kiju@gmail.com", "Passwo12!@");
        String accountNumber = "777123456781";
        return CashWallet.create(user, accountNumber);
    }
}