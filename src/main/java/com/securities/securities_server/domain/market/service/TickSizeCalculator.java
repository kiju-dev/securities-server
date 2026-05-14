package com.securities.securities_server.domain.market.service;

import org.springframework.stereotype.Component;

@Component
public class TickSizeCalculator {

    private static final long PRICE_RANGE_2000 = 2000L;
    private static final long PRICE_RANGE_5000 = 5000L;
    private static final long PRICE_RANGE_20000 = 20000L;
    private static final long PRICE_RANGE_50000 = 50000L;
    private static final long PRICE_RANGE_200000 = 200000L;
    private static final long PRICE_RANGE_500000 = 500000L;

    private static final long TICK_SIZE_1 = 1L;
    private static final long TICK_SIZE_5 = 5L;
    private static final long TICK_SIZE_10 = 10L;
    private static final long TICK_SIZE_50 = 50L;
    private static final long TICK_SIZE_100 = 100L;
    private static final long TICK_SIZE_500 = 500L;
    private static final long TICK_SIZE_1000 = 1000L;

    public long getTickSize(long amount) {
        if (amount < PRICE_RANGE_2000) {
            return TICK_SIZE_1;
        } else if (amount < PRICE_RANGE_5000) {
            return TICK_SIZE_5;
        } else if (amount < PRICE_RANGE_20000) {
            return TICK_SIZE_10;
        } else if (amount < PRICE_RANGE_50000) {
            return TICK_SIZE_50;
        } else if (amount < PRICE_RANGE_200000) {
            return TICK_SIZE_100;
        } else if (amount < PRICE_RANGE_500000) {
            return TICK_SIZE_500;
        } else {
            return TICK_SIZE_1000;
        }
    }

    public long adjustPrice(long amount) {
        long tickSize = getTickSize(amount);
        if (amount % tickSize != 0) {
            return (amount / tickSize) * tickSize;
        } else {
            return amount;
        }
    }
}
