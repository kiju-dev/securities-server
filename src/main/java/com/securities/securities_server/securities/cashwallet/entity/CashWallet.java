package com.securities.securities_server.securities.cashwallet.entity;

import com.securities.securities_server.securities.user.entity.User;
import com.securities.securities_server.global.baseentity.BaseEntity;
import com.securities.securities_server.global.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_ALREADY_SUSPENDED;
import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_NOT_SUSPENDED;
import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_SUSPENDED;
import static com.securities.securities_server.global.exception.ErrorCode.INSUFFICIENT_BALANCE;
import static com.securities.securities_server.global.exception.ErrorCode.INSUFFICIENT_LOCKED_AMOUNT;
import static com.securities.securities_server.global.exception.ErrorCode.INVALID_AMOUNT;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE cash_wallet SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class CashWallet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private long balance; // 잔액

    @Column(nullable = false)
    private long lockedAmount; // 매수 주문으로 묶인 금액

    @Column(nullable = false, name = "is_blocked")
    private boolean blocked; // 계좌 정지 여부

    private CashWallet(
            User user,
            String accountNumber,
            long balance,
            long lockedAmount,
            boolean blocked
    ) {
        this.user = user;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.lockedAmount = lockedAmount;
        this.blocked = blocked;
    }

    public static CashWallet create(
            User user,
            String accountNumber
    ) {
        return new CashWallet(
                user,
                accountNumber,
                0L,
                0L,
                false
        );
    }

    public void deposit(long amount) {
        validateNotBlocked();
        validatePositiveAmount(amount);
        this.balance += amount;
    }

    public void withdraw(long amount) {
        validateNotBlocked();
        validatePositiveAmount(amount);
        validateSufficientBalance(amount);
        this.balance -= amount;
    }

    public void lock(long amount) {
        validateNotBlocked();
        validatePositiveAmount(amount);
        validateSufficientBalance(amount);
        this.lockedAmount += amount;
    }

    public void unlock(long amount) {
        validateNotBlocked();
        validatePositiveAmount(amount);
        validateSufficientLockedAmount(amount);
        this.lockedAmount -= amount;
    }

    public void payLockedAmount(long amount){
        validateNotBlocked();
        validatePositiveAmount(amount);
        validateSufficientLockedAmount(amount);
        this.lockedAmount -= amount;
        this.balance -= amount;
    }

    public long getAvailableAmount() {
        return this.balance - this.lockedAmount;
    }

    public void block() {
        if (this.blocked) {
            throw new CustomException(CASH_WALLET_ALREADY_SUSPENDED);
        }
        this.blocked = true;
    }

    public void unblock() {
        if (!this.blocked) {
            throw new CustomException(CASH_WALLET_NOT_SUSPENDED);
        }
        this.blocked = false;
    }

    public void validateNotBlocked() {
        if (this.blocked) {
            throw new CustomException(CASH_WALLET_SUSPENDED);
        }
    }

    private void validateSufficientBalance(long amount) {
        long availableAmount = getAvailableAmount();
        if (amount > availableAmount) {
            throw new CustomException(INSUFFICIENT_BALANCE);
        }
    }

    private void validateSufficientLockedAmount(long amount) {
        if (amount > this.lockedAmount) {
            throw new CustomException(INSUFFICIENT_LOCKED_AMOUNT);
        }
    }

    private static void validatePositiveAmount(long amount) {
        if (amount <= 0) {
            throw new CustomException(INVALID_AMOUNT);
        }
    }
}
