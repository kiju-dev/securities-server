package com.securities.securities_server.securities.cashwallet.service;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AccountNumberGeneratorTest {

    final AccountNumberGenerator accountNumberGenerator = new AccountNumberGenerator();

    @Test
    void 생성된_계좌번호는_777로_시작한다() {
        // when
        String accountNumber = accountNumberGenerator.generateAccountNumber();

        // then
        assertThat(accountNumber).startsWith("777");
    }

    @Test
    void 생성된_계좌번호는_1로_끝난다() {
        // when
        String accountNumber = accountNumberGenerator.generateAccountNumber();

        // then
        assertThat(accountNumber).endsWith("1");
    }

    @Test
    void 생성된_계좌번호는_12자리이다() {
        // when
        String accountNumber = accountNumberGenerator.generateAccountNumber();

        // then
        assertThat(accountNumber).hasSize(12);
    }
}