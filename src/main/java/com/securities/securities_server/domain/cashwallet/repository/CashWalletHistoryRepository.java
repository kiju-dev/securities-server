package com.securities.securities_server.domain.cashwallet.repository;

import com.securities.securities_server.domain.cashwallet.entity.CashWalletHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashWalletHistoryRepository extends JpaRepository<CashWalletHistory, Long> {
}
