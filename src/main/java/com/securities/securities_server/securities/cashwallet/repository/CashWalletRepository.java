package com.securities.securities_server.securities.cashwallet.repository;

import com.securities.securities_server.securities.cashwallet.entity.CashWallet;
import com.securities.securities_server.securities.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

@Repository
public interface CashWalletRepository extends JpaRepository<CashWallet, Long> {

    boolean existsByUser(User user);

    Optional<CashWallet> findByUserId(Long userId);

    @Lock(PESSIMISTIC_WRITE)
    Optional<CashWallet> findWithLockByUserId(Long userId);

    @Lock(PESSIMISTIC_WRITE)
    Optional<CashWallet> findWithLockById(Long cashWalletId);
}
