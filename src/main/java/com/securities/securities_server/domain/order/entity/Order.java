package com.securities.securities_server.domain.order.entity;

import com.securities.securities_server.domain.stock.entity.Stock;
import com.securities.securities_server.domain.user.entity.User;
import com.securities.securities_server.global.baseentity.BaseEntity;
import com.securities.securities_server.global.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import static com.securities.securities_server.global.exception.ErrorCode.INVALID_MATCH_QUANTITY;
import static com.securities.securities_server.global.exception.ErrorCode.INVALID_QUANTITY;
import static com.securities.securities_server.global.exception.ErrorCode.ORDER_CANCEL_NOT_ALLOWED;
import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE orders SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(STRING)
    @Column(nullable = false)
    private OrderSide side;

    @Column(nullable = false)
    private long price;

    @Column(nullable = false)
    private long quantity;

    @Column(nullable = false)
    private long unfilledQuantity;

    @Column(nullable = false)
    private long canceledQuantity = 0L;

    private Order(
            User user,
            Stock stock,
            OrderSide side,
            long price,
            long quantity,
            long unfilledQuantity
    ) {
        this.user = user;
        this.stock = stock;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.unfilledQuantity = unfilledQuantity;
    }

    public static Order create(
            User user,
            Stock stock,
            OrderSide side,
            long price,
            long quantity
    ) {
        return new Order(
                user,
                stock,
                side,
                price,
                quantity,
                quantity
        );
    }

    public void fill(long quantity) {
        validatePositiveQuantity(quantity);

        if (quantity > this.unfilledQuantity) {
            throw new CustomException(INVALID_MATCH_QUANTITY);
        }
        this.unfilledQuantity -= quantity;
    }

    public void cancelRemainingQuantity() {
        if (this.unfilledQuantity <= 0) {
            throw new CustomException(ORDER_CANCEL_NOT_ALLOWED);
        }
        this.canceledQuantity += this.unfilledQuantity;
        this.unfilledQuantity = 0;
    }

    private void validatePositiveQuantity(long quantity) {
        if (quantity <= 0) {
            throw new CustomException(INVALID_QUANTITY);
        }
    }
}
