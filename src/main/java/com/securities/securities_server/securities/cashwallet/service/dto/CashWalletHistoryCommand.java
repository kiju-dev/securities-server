package com.securities.securities_server.securities.cashwallet.service.dto;

import com.securities.securities_server.securities.cashwallet.entity.CashWallet;
import com.securities.securities_server.securities.cashwallet.entity.CashWalletTxType;

public record CashWalletHistoryCommand(
        CashWallet cashWallet,
        CashWalletTxType txType,
        long txAmount,
        long balanceAfter
) {
}
