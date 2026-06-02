package com.securities.securities_server.securities.stockwallet.entity;

public enum StockWalletTxType {

    BUY_EXECUTED, // 구매 주문 체결

    SELL_ORDER_RESERVED, // 판매 주문
    SELL_ORDER_CANCELED, // 판매 주문 취소
    SELL_EXECUTED, // 판매 주문 체결

    ACCOUNT_SUSPENDED, // 계좌 정지
    ACCOUNT_RESUMED // 계좌 정지 해제
}
