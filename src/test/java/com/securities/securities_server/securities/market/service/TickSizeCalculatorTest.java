package com.securities.securities_server.securities.market.service;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class TickSizeCalculatorTest {

    private final TickSizeCalculator tickSizeCalculator = new TickSizeCalculator();

    @Nested
    class 호가_단위는 {

        @ParameterizedTest
        @ValueSource(longs = {1L, 500L, 1000L, 1500, 1999L})
        void 가격_구간이_2000원_미만이면_호가_단위_1을_반환한다(long amount) {
            // when
            long tickSize = tickSizeCalculator.getTickSize(amount);

            // then
            assertThat(tickSize).isEqualTo(1L);
        }

        @ParameterizedTest
        @ValueSource(longs = {2000L, 2500L, 3700L, 4500L, 4999L})
        void 가격_구간이_2000원_이상_5000원_미만이면_호가_단위_5를_반환한다(long amount) {
            // when
            long tickSize = tickSizeCalculator.getTickSize(amount);

            // then
            assertThat(tickSize).isEqualTo(5L);
        }

        @ParameterizedTest
        @ValueSource(longs = {5000L, 8500L, 10000L, 14500L, 19999L})
        void 가격_구간이_5000원_이상_20000원_미만이면_호가_단위_10을_반환한다(long amount) {
            // when
            long tickSize = tickSizeCalculator.getTickSize(amount);

            // then
            assertThat(tickSize).isEqualTo(10L);
        }

        @ParameterizedTest
        @ValueSource(longs = {20000L, 25000L, 37000L, 45000L, 49999L})
        void 가격_구간이_20000원_이상_50000원_미만이면_호가_단위_50을_반환한다(long amount) {
            // when
            long tickSize = tickSizeCalculator.getTickSize(amount);

            // then
            assertThat(tickSize).isEqualTo(50L);
        }

        @ParameterizedTest
        @ValueSource(longs = {50000L, 62500L, 73700L, 184500L, 199999L})
        void 가격_구간이_50000원_이상_200000원_미만이면_호가_단위_100을_반환한다(long amount) {
            // when
            long tickSize = tickSizeCalculator.getTickSize(amount);

            // then
            assertThat(tickSize).isEqualTo(100L);
        }

        @ParameterizedTest
        @ValueSource(longs = {200000L, 250000L, 370000L, 450000L, 499999L})
        void 가격_구간이_200000원_이상_500000원_미만이면_호가_단위_500를_반환한다(long amount) {
            // when
            long tickSize = tickSizeCalculator.getTickSize(amount);

            // then
            assertThat(tickSize).isEqualTo(500L);
        }

        @ParameterizedTest
        @ValueSource(longs = {500000L, 625000L, 770000L, 845000L, 1000000L})
        void 가격_구간이_500000원_이상이면_호가_단위_1000를_반환한다(long amount) {
            // when
            long tickSize = tickSizeCalculator.getTickSize(amount);

            // then
            assertThat(tickSize).isEqualTo(1000L);
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {1000L, 2000L, 5000L, 20000L, 50000L, 200000L, 500000L})
    void 가격이_호가_단위에_맞으면_그대로_반환한다(long amount) {
        // when
        long adjustAmount = tickSizeCalculator.adjustPrice(amount);

        // then
        assertThat(adjustAmount).isEqualTo(amount);
    }

    @ParameterizedTest
    @CsvSource({
            "2001, 2000",
            "5006, 5000",
            "20015, 20000",
            "50165, 50100",
            "200582, 200500",
            "503591, 503000"
    })
    void 가격이_호가_단위에_맞지_않으면_조정하여_반환한다(
            long amount,
            long expectedAmount
    ) {
        // when
        long adjustedAmount = tickSizeCalculator.adjustPrice(amount);

        // then
        assertThat(adjustedAmount).isEqualTo(expectedAmount);
    }
}