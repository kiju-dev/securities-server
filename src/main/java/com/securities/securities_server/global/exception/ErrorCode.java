package com.securities.securities_server.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_001", "해당 이메일로 가입된 회원이 이미 있습니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "만료된 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_003", "인증이 필요합니다."),

    INTERNAL_SERVER(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_001", "서버 내부에 오류가 발생했습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
