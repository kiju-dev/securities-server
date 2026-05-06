package com.securities.securities_server.domain.cashwallet.service.dto;

import com.securities.securities_server.domain.cashwallet.entity.CashWallet;
import com.securities.securities_server.domain.cashwallet.entity.CashWalletTxType;

public record CashWalletHistoryCommand(
        CashWallet cashWallet,
        CashWalletTxType txType,
        long txAmount,
        long balanceAfter
) {
}
