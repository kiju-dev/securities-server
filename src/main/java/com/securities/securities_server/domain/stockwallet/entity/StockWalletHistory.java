package com.securities.securities_server.domain.stockwallet.entity;

import com.securities.securities_server.global.baseentity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE stock_wallet_history SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class StockWalletHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private StockWallet stockWallet;

    @Column(nullable = false)
    private StockWalletTxType txType;

    @Column(nullable = false)
    private long txAmount;

    @Column(nullable = false)
    private long remainingQuantity;

    private StockWalletHistory(
            StockWallet stockWallet,
            StockWalletTxType txType,
            long txAmount,
            long remainingQuantity
    ) {
        this.stockWallet = stockWallet;
        this.txType = txType;
        this.txAmount = txAmount;
        this.remainingQuantity = remainingQuantity;
    }

    public static StockWalletHistory create(
            StockWallet stockWallet,
            StockWalletTxType txType,
            long txAmount,
            long remainingQuantity
    ) {
        return new StockWalletHistory(
                stockWallet,
                txType,
                txAmount,
                remainingQuantity
        );
    }
}
