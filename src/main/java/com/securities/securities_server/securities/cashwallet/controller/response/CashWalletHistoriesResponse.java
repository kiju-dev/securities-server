package com.securities.securities_server.securities.cashwallet.controller.response;

import java.util.List;

public record CashWalletHistoriesResponse(
    long totalElements,
    List<CashWalletHistoryResponse> cashWalletHistories
) {
}
