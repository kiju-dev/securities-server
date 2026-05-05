package com.securities.securities_server.global.auth;

import java.time.Instant;

public record AccessTokenInfo(
        String accessToken,
        Instant accessTokenExpiredAt
) {
}
