package com.securities.securities_server.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Getter
public enum ErrorCode {
    DUPLICATE_EMAIL(CONFLICT, "USER_001", "해당 이메일로 가입된 회원이 이미 있습니다."),

    INTERNAL_SERVER(INTERNAL_SERVER_ERROR, "SERVER_001", "서버 내부에 오류가 발생했습니다."),
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
