package com.securities.securities_server.domain.cashwallet.service;

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

import static com.securities.securities_server.domain.cashwallet.entity.CashWalletTxType.DEPOSIT;
import static org.assertj.core.api.Assertions.assertThat;
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
        long txAmount = 10000L;
        CashWalletHistoryCommand command =
                new CashWalletHistoryCommand(
                        cashWallet,
                        DEPOSIT,
                        txAmount,
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
        assertThat(savedHistory.getTxAmount()).isEqualTo(txAmount);
        assertThat(savedHistory.getBalanceAfter()).isEqualTo(cashWallet.getBalance());
    }

    private CashWallet createCashWallet() {
        User user = User.signUp("kiju", "kiju@gmail.com", "Passwo12!@");
        String accountNumber = "777123456781";
        return CashWallet.create(user, accountNumber);
    }
}