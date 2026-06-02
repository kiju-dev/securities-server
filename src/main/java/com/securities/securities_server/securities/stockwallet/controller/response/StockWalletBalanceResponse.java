package com.securities.securities_server.securities.stockwallet.controller.response;

import com.securities.securities_server.securities.stockwallet.entity.StockWallet;

public record StockWalletBalanceResponse(
        Long stockWalletId,
        Long stockId,
        long holdingQuantity,
        long lockedQuantity,
        long availableQuantity
) {
    public static StockWalletBalanceResponse from(StockWallet stockWallet) {
        return new StockWalletBalanceResponse(
                stockWallet.getId(),
                stockWallet.getStock().getId(),
                stockWallet.getHoldingQuantity(),
                stockWallet.getLockedQuantity(),
                stockWallet.getAvailableQuantity()
        );
    }
}
