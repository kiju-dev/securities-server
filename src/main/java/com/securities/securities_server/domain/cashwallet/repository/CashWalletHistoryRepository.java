package com.securities.securities_server.domain.cashwallet.repository;

import com.securities.securities_server.domain.cashwallet.entity.CashWallet;
import com.securities.securities_server.domain.cashwallet.entity.CashWalletHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CashWalletHistoryRepository extends JpaRepository<CashWalletHistory, Long> {
    Page<CashWalletHistory> findByCashWallet(CashWallet cashWallet, Pageable pageable);
}
