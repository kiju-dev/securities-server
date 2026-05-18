package com.securities.securities_server.domain.market.entity;

import com.securities.securities_server.domain.market.service.dto.MarketPriceInfo;
import com.securities.securities_server.domain.stock.entity.Stock;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MarketStatusTest {

    @Nested
    class 주문_체결_시 {

        @Test
        void MarketStatus_정보를_업데이트한다() {
            // given
            Stock stock = new Stock("삼성전자", "001123");
            LocalDate now = LocalDate.now();
            MarketPriceInfo marketPriceInfo =
                    new MarketPriceInfo(20000L, 21000L, 19000L);
            MarketStatus marketStatus = MarketStatus.create(stock, now, marketPriceInfo);

            // when
            marketStatus.applyTrade(20500L, 2L);
            marketStatus.applyTrade(21000L, 2L);

            // then
            assertThat(marketStatus.getOpeningPrice()).isEqualTo(20500L);
            assertThat(marketStatus.getClosingPrice()).isEqualTo(21000L);
            assertThat(marketStatus.getHighestPrice()).isEqualTo(21000L);
            assertThat(marketStatus.getLowestPrice()).isEqualTo(20500L);
            assertThat(marketStatus.getTradingVolume()).isEqualTo(4L);
            assertThat(marketStatus.getTradingAmount()).isEqualTo(83000L);
        }
    }
}