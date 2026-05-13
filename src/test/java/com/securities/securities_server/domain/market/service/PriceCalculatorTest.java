package com.securities.securities_server.domain.market.service;

import com.securities.securities_server.domain.market.service.dto.MarketPriceInfo;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PriceCalculatorTest {

    private final TickSizeCalculator tickSizeCalculator = new TickSizeCalculator();
    private final PriceCalculator priceCalculator = new PriceCalculator(tickSizeCalculator);

    @Test
    void 거래량과_거래대금을_사용하여_기준가_및_상하한가를_계산한다() {
        // when
        MarketPriceInfo marketPriceInfo = priceCalculator.calculateMarketPriceInfo(
                30000L,
                10000000L,
                400L
        );

        // then
        assertThat(marketPriceInfo.referencePrice()).isEqualTo(25000L);
        assertThat(marketPriceInfo.upperLimitPrice()).isEqualTo(26250L);
        assertThat(marketPriceInfo.lowerLimitPrice()).isEqualTo(23750L);
    }

    @Test
    void 거래량이_0이면_기준가를_그대로_반환한다() {
        // when
        MarketPriceInfo marketPriceInfo = priceCalculator.calculateMarketPriceInfo(
                30000L,
                0L,
                0L
        );

        // then
        assertThat(marketPriceInfo.referencePrice()).isEqualTo(30000L);
        assertThat(marketPriceInfo.upperLimitPrice()).isEqualTo(31500L);
        assertThat(marketPriceInfo.lowerLimitPrice()).isEqualTo(28500L);
    }

    @Test
    void 기본_기준가로_상하한가를_계산하여_반환한다() {
        // when
        MarketPriceInfo defaultMarketPriceInfo = priceCalculator.getDefaultMarketPriceInfo();

        // then
        assertThat(defaultMarketPriceInfo.referencePrice()).isEqualTo(20000L);
        assertThat(defaultMarketPriceInfo.upperLimitPrice()).isEqualTo(21000L);
        assertThat(defaultMarketPriceInfo.lowerLimitPrice()).isEqualTo(19000L);
    }
}