package com.securities.securities_server.domain.cashwallet.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CashWalletHistory {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "cash_wallet_id", nullable = false)
    private CashWallet cashWallet;

    @Enumerated(STRING)
    @Column(nullable = false)
    private CashWalletTxType txType;

    @Column(nullable = false)
    private long txAmount;

    @Column(nullable = false)
    private long balanceAfter;

    public CashWalletHistory(
            CashWallet cashWallet,
            CashWalletTxType txType,
            long txAmount,
            long balanceAfter
    ) {
        this.cashWallet = cashWallet;
        this.txType = txType;
        this.txAmount = txAmount;
        this.balanceAfter = balanceAfter;
    }

    public static CashWalletHistory createHistory(
            CashWallet cashWallet,
            CashWalletTxType txType,
            long txAmount,
            long balanceAfter
    ) {
        return new CashWalletHistory(
                cashWallet,
                txType,
                txAmount,
                balanceAfter
        );
    }
}
