package com.securities.securities_server.securities.stockwallet.service.dto;

import com.securities.securities_server.securities.stockwallet.entity.StockWallet;
import com.securities.securities_server.securities.stockwallet.entity.StockWalletTxType;

public record StockWalletHistoryCommand(
        StockWallet stockWallet,
        StockWalletTxType txType,
        long txQuantity,
        long holdingQuantityAfter
) {
}
