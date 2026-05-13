package com.securities.securities_server.domain.stockwallet.repository;

import com.securities.securities_server.domain.stockwallet.entity.StockWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockWalletRepository extends JpaRepository<StockWallet, Long> {

    boolean existsByUserIdAndStockId(Long userId, Long stockId);

    Optional<StockWallet> findByIdAndUserId(Long stockWalletId, Long userId);

    Optional<StockWallet> findByUserIdAndStockId(Long userId, Long stockId);
}
