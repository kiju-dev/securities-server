package com.securities.securities_server.domain.cashwallet.repository;

import com.securities.securities_server.domain.cashwallet.entity.CashWallet;
import com.securities.securities_server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CashWalletRepository extends JpaRepository<CashWallet, Long> {

    Optional<CashWallet> findByUser(User user);
}
