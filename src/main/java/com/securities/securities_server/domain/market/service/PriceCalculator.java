package com.securities.securities_server.domain.market.service;

import com.securities.securities_server.domain.market.service.dto.MarketPriceInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class PriceCalculator {

    private final TickSizeCalculator tickSizeCalculator;

    private static final BigDecimal PRICE_UPPER_LIMIT_RATE = BigDecimal.valueOf(1.05);
    private static final BigDecimal PRICE_LOWER_LIMIT_RATE = BigDecimal.valueOf(0.95);
    private static final long DEFAULT_REFERENCE_PRICE = 20000L;

    public MarketPriceInfo calculateMarketPriceInfo(
            long referencePrice,
            long tradingAmount,
            long tradingVolume
    ) {
        long nextReferencePrice = calculateReferencePrice(referencePrice, tradingAmount, tradingVolume);
        long upperLimitPrice = calculateUpperLimitPrice(nextReferencePrice);
        long lowerLimitPrice = calculateLowerLimitPrice(nextReferencePrice);

        return new MarketPriceInfo(
                nextReferencePrice,
                upperLimitPrice,
                lowerLimitPrice
        );
    }

    public MarketPriceInfo getDefaultMarketPriceInfo() {
        return new MarketPriceInfo(
                DEFAULT_REFERENCE_PRICE,
                calculateUpperLimitPrice(DEFAULT_REFERENCE_PRICE),
                calculateLowerLimitPrice(DEFAULT_REFERENCE_PRICE)
        );
    }

    private long calculateReferencePrice(
            long referencePrice,
            long tradingAmount,
            long tradingVolume
    ) {
        if (tradingVolume == 0) {
            return referencePrice;
        }
        return tickSizeCalculator.adjustPrice(tradingAmount / tradingVolume);
    }

    private long calculateUpperLimitPrice(long referencePrice) {
        long upperLimitPrice = BigDecimal.valueOf(referencePrice)
                .multiply(PRICE_UPPER_LIMIT_RATE)
                .longValue();
        return tickSizeCalculator.adjustPrice(upperLimitPrice);
    }

    private long calculateLowerLimitPrice(long referencePrice) {
        long lowerLimitPrice = BigDecimal.valueOf(referencePrice)
                .multiply(PRICE_LOWER_LIMIT_RATE)
                .longValue();
        return tickSizeCalculator.adjustPrice(lowerLimitPrice);
    }
}
