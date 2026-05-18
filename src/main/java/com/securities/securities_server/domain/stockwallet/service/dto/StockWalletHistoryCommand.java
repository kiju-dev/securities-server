package com.securities.securities_server.domain.stockwallet.service.dto;

import com.securities.securities_server.domain.stockwallet.entity.StockWallet;
import com.securities.securities_server.domain.stockwallet.entity.StockWalletTxType;

public record StockWalletHistoryCommand(
        StockWallet stockWallet,
        StockWalletTxType txType,
        long txQuantity,
        long holdingQuantityAfter
) {
}
