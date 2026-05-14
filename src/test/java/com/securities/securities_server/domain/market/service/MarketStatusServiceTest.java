package com.securities.securities_server.domain.market.service;

import com.securities.securities_server.domain.market.entity.MarketStatus;
import com.securities.securities_server.domain.market.repository.MarketStatusRepository;
import com.securities.securities_server.domain.market.service.dto.MarketPriceInfo;
import com.securities.securities_server.domain.stock.entity.Stock;
import com.securities.securities_server.domain.stock.repository.StockRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MarketStatusServiceTest {

    @Mock
    MarketStatusRepository marketStatusRepository;
    @Mock
    StockRepository stockRepository;
    @Mock
    PriceCalculator priceCalculator;

    @InjectMocks
    MarketStatusService marketStatusService;

    @Test
    void 당일_MarketStatus가_존재하면_계산된_시세로_다음날_MarketStatus를_생성한다() {
        // given
        Stock stock = new Stock("삼성전자", "001123");
        LocalDate tradingDate = LocalDate.now();
        MarketPriceInfo marketPriceInfo =
                new MarketPriceInfo(30000L, 31500L, 28500L);
        MarketStatus todayMarketStatus = MarketStatus.create(
                stock,
                tradingDate,
                marketPriceInfo
        );
        given(stockRepository.findAll()).willReturn(List.of(stock));
        given(marketStatusRepository.findByStockIdAndTradingDate(stock.getId(), tradingDate))
                .willReturn(Optional.of(todayMarketStatus));
        MarketPriceInfo calculatedPriceInfo = new MarketPriceInfo(
                30000L,
                31500L,
                28500L
        );
        given(priceCalculator.calculateMarketPriceInfo(30000L, 0L, 0L))
                .willReturn(calculatedPriceInfo);

        // when
        marketStatusService.createNextDayMarketStatuses();

        // then
        verify(priceCalculator).calculateMarketPriceInfo(30000L, 0L, 0L);
        ArgumentCaptor<MarketStatus> captor =
                ArgumentCaptor.forClass(MarketStatus.class);
        verify(marketStatusRepository).save(captor.capture());

        MarketStatus savedMarketStatus = captor.getValue();
        assertThat(savedMarketStatus.getTradingDate()).isEqualTo(tradingDate.plusDays(1));
        assertThat(savedMarketStatus.getReferencePrice()).isEqualTo(30000L);
        assertThat(savedMarketStatus.getUpperLimitPrice()).isEqualTo(31500L);
        assertThat(savedMarketStatus.getLowerLimitPrice()).isEqualTo(28500L);
    }

    @Test

    void 당일_MarketStatus가_존재하지_않으면_기본_시세로_다음날_MarketStatus를_생성한다() {
        // given
        Stock stock = new Stock("삼성전자", "001123");
        LocalDate tradingDate = LocalDate.now();

        given(stockRepository.findAll()).willReturn(List.of(stock));
        given(marketStatusRepository.findByStockIdAndTradingDate(stock.getId(), tradingDate))
                .willReturn(Optional.empty());

        MarketPriceInfo defaultPriceInfo = new MarketPriceInfo(
                20000L,
                21000L,
                19000L
        );
        given(priceCalculator.getDefaultMarketPriceInfo()).willReturn(defaultPriceInfo);
        given(priceCalculator.calculateMarketPriceInfo(20000L, 0L, 0L))
                .willReturn(defaultPriceInfo);

        // when
        marketStatusService.createNextDayMarketStatuses();

        // then
        verify(priceCalculator).getDefaultMarketPriceInfo();
        verify(priceCalculator).calculateMarketPriceInfo(20000L, 0L, 0L);

        ArgumentCaptor<MarketStatus> captor = ArgumentCaptor.forClass(MarketStatus.class);
        verify(marketStatusRepository).save(captor.capture());

        MarketStatus savedMarketStatus = captor.getValue();
        assertThat(savedMarketStatus.getTradingDate()).isEqualTo(tradingDate.plusDays(1));
        assertThat(savedMarketStatus.getReferencePrice()).isEqualTo(20000L);
        assertThat(savedMarketStatus.getUpperLimitPrice()).isEqualTo(21000L);
        assertThat(savedMarketStatus.getLowerLimitPrice()).isEqualTo(19000L);
    }
}