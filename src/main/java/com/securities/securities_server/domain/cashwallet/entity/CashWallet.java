package com.securities.securities_server.domain.cashwallet.entity;

import com.securities.securities_server.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CashWallet {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    @Column(nullable = false)
    private long reserve; // 예치금

    @Column(nullable = false)
    private long deposit; // 매수 주문으로 묶인 금액

    @Column(nullable = false, name = "is_blocked")
    private boolean blocked; // 계좌 정지 여부

    private CashWallet(
            User user,
            String accountNumber,
            long reserve,
            long deposit,
            boolean isBlocked
    ) {
        this.user = user;
        this.accountNumber = accountNumber;
        this.reserve = reserve;
        this.deposit = deposit;
        this.blocked = isBlocked;
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
}
