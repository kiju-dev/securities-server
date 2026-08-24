package com.securities.securities_server.securities.cashwallet.repository;

import com.securities.securities_server.securities.cashwallet.entity.CashWallet;
import com.securities.securities_server.securities.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CashWalletRepository extends JpaRepository<CashWallet, Long> {

    boolean existsByUser(User user);

    Optional<CashWallet> findByUserId(Long userId);
}
