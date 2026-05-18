package com.securities.securities_server.domain.match.entity;

import com.securities.securities_server.domain.order.entity.Order;
import com.securities.securities_server.global.baseentity.BaseEntity;
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
@SQLDelete(sql = "UPDATE match SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Match extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "maker_order_id", nullable = false)
    private Order makerOrder;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "taker_order_id", nullable = false)
    private Order takerOrder;

    private Match(Order makerOrder, Order takerOrder) {
        this.makerOrder = makerOrder;
        this.takerOrder = takerOrder;
    }

    public static Match create(Order makerOrder, Order takerOrder) {
        return new Match(makerOrder, takerOrder);
    }
}
