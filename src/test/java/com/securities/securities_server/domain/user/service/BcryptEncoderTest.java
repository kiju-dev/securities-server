package com.securities.securities_server.domain.user.service;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class BcryptEncoderTest {

    private final BcryptEncoder bcryptEncoder = new BcryptEncoder();

    @Test
    void Bcrypt_알고리즘을_사용해_암호화_된_비밀번호를_반환한다() {
        // given
        String rawPassword = "Password12!@";

        // when
        String encodedPassword = bcryptEncoder.encode(rawPassword);

        // then
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(encodedPassword.length()).isEqualTo(60);
    }

    @Test
    void Bcrypt_알고리즘을_사용해_암호환된_비밀번호가_평문_비밀번호와_일치하면_True를_반환한다() {
        // given
        String rawPassword = "Password12!@";
        String encodedPassword = bcryptEncoder.encode(rawPassword);

        // when
        boolean matches = bcryptEncoder.matches(rawPassword, encodedPassword);

        // then
        assertThat(matches).isTrue();
    }

    @Test
    void Bcrypt_알고리즘을_사용해_암호환된_비밀번호가_평문_비밀번호와_일치하지_않으면_false를_반환한다() {
        // given
        String rawPassword = "Password12!@";
        String encodedPassword = bcryptEncoder.encode("WrongPassword");

        // when
        boolean matches = bcryptEncoder.matches(rawPassword, encodedPassword);

        // then
        assertThat(matches).isFalse();
    }
}