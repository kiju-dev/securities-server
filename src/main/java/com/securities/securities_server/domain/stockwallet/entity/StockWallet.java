package com.securities.securities_server.domain.stockwallet.entity;

import com.securities.securities_server.domain.stock.entity.Stock;
import com.securities.securities_server.domain.user.entity.User;
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
@SQLDelete(sql = "UPDATE stock_wallet SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class StockWallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private long holdingQuantity;

    @Column(nullable = false)
    private long lockedQuantity;

    @Column(nullable = false)
    private boolean blocked;

    private StockWallet(
            User user,
            Stock stock,
            long holdingQuantity,
            long lockedQuantity,
            boolean blocked
    ) {
        this.user = user;
        this.stock = stock;
        this.holdingQuantity = holdingQuantity;
        this.lockedQuantity = lockedQuantity;
        this.blocked = blocked;
    }

    public static StockWallet create(User user, Stock stock) {
        return new StockWallet(
                user,
                stock,
                0L,
                0L,
                false
        );
    }
}
