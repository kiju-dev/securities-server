package com.securities.securities_server.domain.user.controller.response;

import com.securities.securities_server.global.auth.AccessTokenInfo;

import java.time.Instant;

public record LoginResponse(
        String accessToken,
        Instant accessTokenExpiredAt
) {
    public static LoginResponse of(AccessTokenInfo accessTokenInfo) {
        return new LoginResponse(
                accessTokenInfo.accessToken(),
                accessTokenInfo.accessTokenExpiredAt()
        );
    }
}
