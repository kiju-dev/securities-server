package com.securities.securities_server.global.auth;

import com.securities.securities_server.global.exception.CustomException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

import static com.securities.securities_server.global.exception.ErrorCode.EXPIRED_TOKEN;
import static com.securities.securities_server.global.exception.ErrorCode.INVALID_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JwtProviderTest {

    static final String TEST_SECRET_KEY = "a".repeat(64);

    final JwtProvider jwtProvider = new JwtProvider(TEST_SECRET_KEY);

    @Test
    void AccessToken을_생성할_수_있다() {
        // given
        Long userId = 1L;

        // when
        String accessToken = jwtProvider.createAccessToken(userId);

        // then
        assertThat(accessToken).isNotNull();
    }

    @Test
    void AccessToken에서_userId를_얻을_수_있다() {
        // given
        Long userId = 1L;
        String accessToken = jwtProvider.createAccessToken(userId);

        // when
        Long tokenUserId = jwtProvider.getUserId(accessToken);

        // then
        assertThat(tokenUserId).isEqualTo(userId);
    }

    @Test
    void AccessToken을_검증할_수_있다() {
        // given
        Long userId = 1L;
        String accessToken = jwtProvider.createAccessToken(userId);

        // when & then
        assertThatCode(() -> jwtProvider.validate(accessToken))
                .doesNotThrowAnyException();
    }

    @Test
    void AccessToken이_만료되면_EXPIRED_TOKEN_예외가_발생한다() {
        // given
        Date date = new Date();
        String token = Jwts.builder()
                .subject("1L")
                .expiration(date)
                .signWith(Keys.hmacShaKeyFor(TEST_SECRET_KEY.getBytes(StandardCharsets.UTF_8)))
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtProvider.validate(token))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(EXPIRED_TOKEN);
    }

    @Test
    void AccessToken의_형식이_잘못되면_INVALID_TOKEN_예외가_발생한다() {
        // given
        Long userId = 1L;
        String accessToken = jwtProvider.createAccessToken(userId);
        String noBearerToken = accessToken.substring(7);

        // when
        assertThatThrownBy(() -> jwtProvider.validate(noBearerToken))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_TOKEN);
    }

    @Test
    void AccessToken의_서명이_틀린경우_INVALID_TOKEN_예외가_발생한다() {
        // given
        Long userId = 1L;
        String accessToken = jwtProvider.createAccessToken(userId);
        String[] parts = accessToken.split("\\.");

        String wrongPayload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("""
                        {"sub":"999L}
                        """.getBytes(StandardCharsets.UTF_8));
        String wrongToken = parts[0] + "." + wrongPayload + "." + parts[2];

        // when
        assertThatThrownBy(() -> jwtProvider.validate(wrongToken))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(INVALID_TOKEN);

    }
}