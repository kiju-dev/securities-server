package com.securities.securities_server.domain.stockwallet.repository;

import com.securities.securities_server.domain.stockwallet.entity.StockWalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockWalletHistoryRepository extends JpaRepository<StockWalletHistory, Long> {
}
