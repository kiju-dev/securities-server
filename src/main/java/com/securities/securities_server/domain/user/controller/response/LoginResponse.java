package com.securities.securities_server.domain.user.controller.response;

import com.securities.securities_server.global.auth.TokenInfo;

import java.time.Instant;

public record LoginResponse(
        String accessToken,
        Instant accessTokenExpiredAt
) {
    public static LoginResponse of(TokenInfo tokenInfo) {
        return new LoginResponse(
                tokenInfo.accessToken(),
                tokenInfo.accessTokenExpiredAt()
        );
    }
}
