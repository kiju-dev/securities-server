package com.securities.securities_server.domain.cashwallet.controller.response;

import com.securities.securities_server.domain.cashwallet.entity.CashWalletHistory;
import com.securities.securities_server.domain.cashwallet.entity.CashWalletTxType;

import java.time.LocalDateTime;

public record CashWalletHistoryResponse(
        Long historyId,
        CashWalletTxType txType,
        long txAmount,
        long balanceAfter,
        LocalDateTime createdAt
) {
    public static CashWalletHistoryResponse from(CashWalletHistory history) {
        return new CashWalletHistoryResponse(
                history.getId(),
                history.getTxType(),
                history.getTxAmount(),
                history.getBalanceAfter(),
                history.getCreatedAt()
        );
    }
}
