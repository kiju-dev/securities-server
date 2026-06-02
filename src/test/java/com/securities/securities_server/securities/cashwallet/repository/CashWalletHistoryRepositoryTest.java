package com.securities.securities_server.securities.cashwallet.repository;

import com.securities.securities_server.securities.cashwallet.entity.CashWallet;
import com.securities.securities_server.securities.cashwallet.entity.CashWalletHistory;
import com.securities.securities_server.securities.user.entity.User;
import com.securities.securities_server.securities.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static com.securities.securities_server.securities.cashwallet.entity.CashWalletTxType.DEPOSIT;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CashWalletHistoryRepositoryTest {

    @Autowired
    CashWalletHistoryRepository cashWalletHistoryRepository;

    @Autowired
    private CashWalletRepository cashWalletRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void 현금_계좌_내역을_페이징해서_조회한다() {
        // given
        User user = userRepository.save(
                User.signUp("kiju", "kiju@gmail.com", "Pass12!@")
        );

        CashWallet cashWallet1 = cashWalletRepository.save(
                CashWallet.create(user, "777123456781")
        );

        cashWalletHistoryRepository.save(
                CashWalletHistory.create(cashWallet1, DEPOSIT, 10000L, 20000L)
        );
        cashWalletHistoryRepository.save(
                CashWalletHistory.create(cashWallet1, DEPOSIT, 30000L, 50000L)
        );
        Pageable pageable = PageRequest.of(0, 1);

        // when
        Page<CashWalletHistory> histories =
                cashWalletHistoryRepository.findByCashWallet(cashWallet1, pageable);

        // then
        assertThat(histories.getTotalElements()).isEqualTo(2);
        assertThat(histories.getContent()).hasSize(1);
        assertThat(histories.getTotalPages()).isEqualTo(2);
        assertThat(histories.getContent().getFirst().getCashWallet()).isEqualTo(cashWallet1);
    }
}