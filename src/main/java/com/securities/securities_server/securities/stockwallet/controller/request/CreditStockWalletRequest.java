package com.securities.securities_server.securities.stockwallet.controller.request;

public record CreditStockWalletRequest(
        Long stockWalletId,
        long quantity
) {
}
