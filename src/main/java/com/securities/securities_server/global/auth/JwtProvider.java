package com.securities.securities_server.global.auth;

import com.securities.securities_server.global.exception.CustomException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static com.securities.securities_server.global.exception.ErrorCode.EXPIRED_TOKEN;
import static com.securities.securities_server.global.exception.ErrorCode.INVALID_TOKEN;

@Component
public class JwtProvider {

    private final SecretKey secretKey;

    public JwtProvider(@Value("${jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public AccessTokenInfo createAccessToken(Long userId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(Duration.ofMinutes(5));

        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();

        return new AccessTokenInfo(token, expiration);
    }

    public RefreshTokenInfo createRefreshToken(Long userId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(Duration.ofDays(7));

        String token = Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
        return new RefreshTokenInfo(token, expiration);
    }

    public Long getUserId(String token) {
        return Long.valueOf(
                Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getSubject()
        );
    }

    public void validate(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(EXPIRED_TOKEN);
        } catch (JwtException e) {
            throw new CustomException(INVALID_TOKEN);
        }
    }
}
