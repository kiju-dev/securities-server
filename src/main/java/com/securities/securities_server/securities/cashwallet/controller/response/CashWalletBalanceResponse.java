package com.securities.securities_server.securities.cashwallet.controller.response;

public record CashWalletBalanceResponse(
        long balance,
        long lockedAmount,
        long availableAmount
) {
}
