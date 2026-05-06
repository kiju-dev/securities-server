package com.securities.securities_server.domain.cashwallet.entity;

public enum CashWalletTxType {

    DEPOSIT, // 입금
    WITHDRAW, // 출금

    TRADE_PAY, // 거래 대금 지급(거래 출금)
    TRADE_RECEIVE, // 거래 대금 수령(거래 입금)
    TRADE_REFUND, // 거래 대금 반환(주문 취소)

    ACCOUNT_SUSPENDED, // 계좌 정지
    ACCOUNT_RESUMED // 계좌 정지 해제
}
