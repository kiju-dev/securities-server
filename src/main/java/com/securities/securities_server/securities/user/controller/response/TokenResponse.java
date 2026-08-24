package com.securities.securities_server.securities.user.controller.response;

import com.securities.securities_server.global.auth.AccessTokenInfo;
import com.securities.securities_server.global.auth.RefreshTokenInfo;

public record TokenResponse(
        AccessTokenInfo accessTokenInfo,
        RefreshTokenInfo refreshTokenInfo
) {
    public static TokenResponse of(AccessTokenInfo accessTokenInfo, RefreshTokenInfo refreshTokenInfo) {
        return new TokenResponse(
                accessTokenInfo,
                refreshTokenInfo
        );
    }
}
