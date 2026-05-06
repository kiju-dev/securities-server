package com.securities.securities_server.domain.cashwallet.service;

import com.securities.securities_server.domain.cashwallet.entity.CashWalletHistory;
import com.securities.securities_server.domain.cashwallet.repository.CashWalletHistoryRepository;
import com.securities.securities_server.domain.cashwallet.service.dto.CashWalletHistoryCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CashWalletHistoryService {

    private final CashWalletHistoryRepository cashWalletHistoryRepository;

    @Transactional
    public void createCashWalletHistory(CashWalletHistoryCommand command) {
        CashWalletHistory history = CashWalletHistory.createHistory(
                command.cashWallet(),
                command.txType(),
                command.txAmount(),
                command.balanceAfter()
        );
        cashWalletHistoryRepository.save(history);
    }
}
