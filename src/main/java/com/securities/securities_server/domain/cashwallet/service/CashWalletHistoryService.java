package com.securities.securities_server.domain.cashwallet.service;

import com.securities.securities_server.domain.cashwallet.controller.response.CashWalletHistoriesResponse;
import com.securities.securities_server.domain.cashwallet.controller.response.CashWalletHistoryResponse;
import com.securities.securities_server.domain.cashwallet.entity.CashWallet;
import com.securities.securities_server.domain.cashwallet.entity.CashWalletHistory;
import com.securities.securities_server.domain.cashwallet.repository.CashWalletHistoryRepository;
import com.securities.securities_server.domain.cashwallet.service.dto.CashWalletHistoryCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CashWalletHistoryService {

    private final CashWalletHistoryRepository cashWalletHistoryRepository;

    @Transactional
    public void createCashWalletHistory(CashWalletHistoryCommand command) {
        CashWalletHistory history = CashWalletHistory.create(
                command.cashWallet(),
                command.txType(),
                command.txAmount(),
                command.balanceAfter()
        );
        cashWalletHistoryRepository.save(history);
    }

    public CashWalletHistoriesResponse getHistories(CashWallet cashWallet, Pageable pageable) {
        Page<CashWalletHistoryResponse> histories =
                cashWalletHistoryRepository.findByCashWallet(cashWallet, pageable)
                        .map(CashWalletHistoryResponse::from);

        return new CashWalletHistoriesResponse(
                histories.getTotalElements(),
                histories.getContent()
        );
    }
}
