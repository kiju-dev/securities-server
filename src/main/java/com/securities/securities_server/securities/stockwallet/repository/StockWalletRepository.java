package com.securities.securities_server.securities.stockwallet.repository;

import com.securities.securities_server.securities.stockwallet.entity.StockWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

@Repository
public interface StockWalletRepository extends JpaRepository<StockWallet, Long> {

    boolean existsByUserIdAndStockId(Long userId, Long stockId);

    Optional<StockWallet> findByIdAndUserId(Long stockWalletId, Long userId);

    Optional<StockWallet> findByUserIdAndStockId(Long userId, Long stockId);

    @Lock(PESSIMISTIC_WRITE)
    Optional<StockWallet> findWithLockByUserIdAndStockId(Long userId, Long stockId);

    @Lock(PESSIMISTIC_WRITE)
    Optional<StockWallet> findWithLockByIdAndUserId(Long stockWalletId, Long userId);

    @Lock(PESSIMISTIC_WRITE)
    Optional<StockWallet> findWithLockById(Long stockWalletId);
}
