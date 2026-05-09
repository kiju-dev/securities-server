package com.securities.securities_server.domain.cashwallet.controller.response;

public record CashWalletBalanceResponse(
        long balance,
        long lockedAmount,
        long availableAmount
) {
}
