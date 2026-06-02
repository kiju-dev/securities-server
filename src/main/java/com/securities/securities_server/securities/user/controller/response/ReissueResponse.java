package com.securities.securities_server.securities.user.controller.response;

import com.securities.securities_server.global.auth.AccessTokenInfo;

import java.time.Instant;

public record ReissueResponse(
        String accessToken,
        Instant accessTokenExpiredAt
) {
    public static ReissueResponse of(AccessTokenInfo accessTokenInfo) {
        return new ReissueResponse(
                accessTokenInfo.accessToken(),
                accessTokenInfo.accessTokenExpiredAt()
        );
    }
}
