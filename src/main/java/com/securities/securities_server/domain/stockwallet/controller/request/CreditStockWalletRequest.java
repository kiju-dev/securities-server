package com.securities.securities_server.domain.stockwallet.controller.request;

public record CreditStockWalletRequest(
        Long stockWalletId,
        long quantity
) {
}
