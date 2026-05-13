package com.securities.securities_server.domain.market.entity;

import com.securities.securities_server.global.baseentity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE market_status SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class MarketStatus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate tradingDate;

    @Column(nullable = false)
    private long referencePrice;

    @Column(nullable = false)
    private long upperLimitPrice;

    @Column(nullable = false)
    private long lowerLimitPrice;

    private long openingPrice = 0L;

    private long closingPrice = 0L;

    private long highestPrice = 0L;

    private long lowestPrice = 0L;

    private long tradingVolume = 0L;

    private long tradingAmount = 0L;

    private MarketStatus(
            LocalDate tradingDate,
            long referencePrice,
            long upperLimitPrice,
            long lowerLimitPrice
    ) {
        this.tradingDate = tradingDate;
        this.referencePrice = referencePrice;
        this.upperLimitPrice = upperLimitPrice;
        this.lowerLimitPrice = lowerLimitPrice;
    }

    public static MarketStatus create(
            LocalDate tradingDate,
            long referencePrice,
            long upperLimitPrice,
            long lowerLimitPrice
    ) {
        return new MarketStatus(
                tradingDate,
                referencePrice,
                upperLimitPrice,
                lowerLimitPrice
        );
    }
}
