package com.securities.securities_server.securities.stockwallet.service;

import com.securities.securities_server.securities.stockwallet.entity.StockWalletHistory;
import com.securities.securities_server.securities.stockwallet.repository.StockWalletHistoryRepository;
import com.securities.securities_server.securities.stockwallet.service.dto.StockWalletHistoryCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockWalletHistoryService {

    private final StockWalletHistoryRepository stockWalletHistoryRepository;

    @Transactional
    public void createStockWalletHistory(StockWalletHistoryCommand command) {
        StockWalletHistory history = StockWalletHistory.create(
                command.stockWallet(),
                command.txType(),
                command.txQuantity(),
                command.holdingQuantityAfter()
        );
        stockWalletHistoryRepository.save(history);
    }
}
