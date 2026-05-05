package com.securities.securities_server.global.auth;

import java.time.Instant;

public record TokenInfo(
        String accessToken,
        Instant accessTokenExpiredAt
) {
}
