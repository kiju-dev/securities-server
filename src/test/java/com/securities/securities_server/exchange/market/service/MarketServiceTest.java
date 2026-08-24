package com.securities.securities_server.exchange.market.service;

import com.securities.securities_server.exchange.market.ExchangeState;
import com.securities.securities_server.exchange.order.orderbook.OrderBookStore;
import com.securities.securities_server.exchange.market.dto.response.MarketCloseResponse;
import com.securities.securities_server.exchange.market.dto.response.MarketOpenResponse;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.securities.securities_server.global.common.ExchangeStatus.RUNNING;
import static com.securities.securities_server.global.common.ExchangeStatus.STOPPED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MarketServiceTest {

    @Mock
    private ExchangeState exchangeState;

    @Mock
    private OrderBookStore orderBookStore;

    @InjectMocks
    private ExchangeMarketService exchangeMarketService;

    @Test
    void 시장을_연다() {
        // given
        given(exchangeState.getStatus()).willReturn(RUNNING);

        // when
        MarketOpenResponse response = exchangeMarketService.openMarket();

        // then
        assertThat(response.exchangeStatus()).isEqualTo(RUNNING);
        assertThat(response.openedAt()).isNotNull();
        verify(exchangeState).open();
        verify(exchangeState).getStatus();
    }

    @Test
    void 시장을_닫는다() {
        // given
        given(exchangeState.getStatus()).willReturn(STOPPED);

        // when
        MarketCloseResponse response = exchangeMarketService.closeMarket();

        // then
        assertThat(response.exchangeStatus()).isEqualTo(STOPPED);
        assertThat(response.closedAt()).isNotNull();
        verify(exchangeState).close();
        verify(exchangeState).getStatus();
    }

}