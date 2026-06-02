package com.securities.securities_server.securities.stockwallet.entity;

import com.securities.securities_server.securities.stock.entity.Stock;
import com.securities.securities_server.securities.user.entity.User;
import com.securities.securities_server.global.baseentity.BaseEntity;
import com.securities.securities_server.global.exception.CustomException;
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

import static com.securities.securities_server.global.exception.ErrorCode.INSUFFICIENT_HOLDING_QUANTITY;
import static com.securities.securities_server.global.exception.ErrorCode.INSUFFICIENT_LOCKED_QUANTITY;
import static com.securities.securities_server.global.exception.ErrorCode.INVALID_QUANTITY;
import static com.securities.securities_server.global.exception.ErrorCode.STOCK_WALLET_ALREADY_SUSPENDED;
import static com.securities.securities_server.global.exception.ErrorCode.STOCK_WALLET_NOT_SUSPENDED;
import static com.securities.securities_server.global.exception.ErrorCode.STOCK_WALLET_SUSPENDED;
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

    public void credit(long quantity) {
        validateNotBlocked();
        validatePositiveQuantity(quantity);

        this.holdingQuantity += quantity;
    }

    public void debit(long quantity) {
        validateNotBlocked();
        validatePositiveQuantity(quantity);

        this.holdingQuantity -= quantity;
    }

    public void lock(long quantity) {
        validateNotBlocked();
        validatePositiveQuantity(quantity);
        if (quantity > getAvailableQuantity()) {
            throw new CustomException(INSUFFICIENT_HOLDING_QUANTITY);
        }
        this.lockedQuantity += quantity;
    }

    public void unlock(long quantity) {
        validateNotBlocked();
        validatePositiveQuantity(quantity);
        validateSufficientLockedQuantity(quantity);
        this.lockedQuantity -= quantity;
    }

    public void sellLockedQuantity(long quantity) {
        validateNotBlocked();
        validatePositiveQuantity(quantity);
        validateSufficientLockedQuantity(quantity);
        this.lockedQuantity -= quantity;
        this.holdingQuantity -= quantity;
    }

    public long getAvailableQuantity() {
        return this.holdingQuantity - this.lockedQuantity;
    }

    public void block() {
        if (this.blocked) {
            throw new CustomException(STOCK_WALLET_ALREADY_SUSPENDED);
        }
        this.blocked = true;
    }

    public void unblock() {
        if (!this.blocked) {
            throw new CustomException(STOCK_WALLET_NOT_SUSPENDED);
        }
        this.blocked = false;
    }

    public void validateNotBlocked() {
        if (this.blocked) {
            throw new CustomException(STOCK_WALLET_SUSPENDED);
        }
    }

    private void validatePositiveQuantity(long quantity) {
        if (quantity <= 0) {
            throw new CustomException(INVALID_QUANTITY);
        }
    }

    private void validateSufficientLockedQuantity(long quantity) {
        if (quantity > this.lockedQuantity) {
            throw new CustomException(INSUFFICIENT_LOCKED_QUANTITY);
        }
    }
}
