package com.securities.securities_server.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USER_001", "해당 이메일로 가입된 회원이 이미 있습니다."),
    INVALID_CREDENTIAL(HttpStatus.UNAUTHORIZED, "USER_002", "이메일 또는 비밀번호가 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_003", "회원을 찾을 수 없습니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "만료된 토큰입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_003", "인증이 필요합니다."),

    INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "CASH_001", "금액은 0보다 커야 합니다."),
    CASH_WALLET_SUSPENDED(HttpStatus.FORBIDDEN, "CASH_002", "현금 계좌가 정지 상태입니다."),
    CASH_WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "CASH_003", "현금 계좌를 찾을 수 없습니다."),
    CASH_WALLET_ALREADY_SUSPENDED(HttpStatus.CONFLICT, "CASH_004", "이미 정지된 현금 계좌입니다."),
    CASH_WALLET_NOT_SUSPENDED(HttpStatus.CONFLICT, "CASH_005", "이미 정지가 해제된 현금 계좌입니다."),
    CASH_WALLET_ALREADY_EXISTS(HttpStatus.CONFLICT, "CASH_006", "이미 개설된 현금 계좌가 존재합니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "CASH_007", "잔액이 부족합니다."),

    STOCK_WALLET_ALREADY_EXISTS(HttpStatus.CONFLICT, "STOCK_WALLET_001", "이미 개설된 종목 계좌가 존재합니다."),
    STOCK_WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK_WALLET_002", "종목 계좌를 찾을 수 없습니다."),
    STOCK_WALLET_SUSPENDED(HttpStatus.FORBIDDEN, "STOCK_WALLET_003", "종목 계좌가 정지 상태입니다."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "STOCK_WALLET_004", "수량은 0보다 커야 합니다."),
    STOCK_WALLET_ALREADY_SUSPENDED(HttpStatus.CONFLICT, "STOCK_WALLET_005", "이미 정지된 종목 계좌입니다."),
    STOCK_WALLET_NOT_SUSPENDED(HttpStatus.CONFLICT, "STOCK_WALLET_006", "이미 정지가 해제된 종목 계좌입니다."),

    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK_001", "종목을 찾을 수 없습니다."),

    MARKET_ALREADY_OPEN(HttpStatus.CONFLICT, "MARKET_001", "이미 개장된 장입니다."),

    INTERNAL_SERVER(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_001", "서버 내부에 오류가 발생했습니다."),

    EXCHANGE_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "EXCHANGE_SERVER_001", "거래소 서버 내부에 오류가 발생했습니다."),
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
