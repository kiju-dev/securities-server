package com.securities.securities_server.global.auth;

import java.time.Instant;

public record RefreshTokenInfo(
        String refreshToken,
        Instant refreshTokenExpiredAt
) {
}
