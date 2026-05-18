package com.securities.securities_server.domain.market.service;

import com.securities.securities_server.global.external.client.ExchangeClient;
import com.securities.securities_server.domain.market.controller.response.MarketOpenResponse;
import com.securities.securities_server.global.exception.CustomException;
import feign.FeignException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static com.securities.securities_server.domain.market.entity.ExchangeStatus.RUNNING;
import static com.securities.securities_server.global.exception.ErrorCode.MARKET_ALREADY_OPEN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MarketServiceTest {

    @Mock
    ExchangeClient exchangeClient;

    @InjectMocks
    MarketService marketService;

    @Test
    void 거래소_장을_시작시키고_거래소_상태와_개장_시각을_반환한다() {
        // given
        LocalDateTime openedAt = LocalDateTime.of(2026, 5, 13, 9, 0);
        MarketOpenResponse exchangeResponse = new MarketOpenResponse(RUNNING, openedAt);
        given(exchangeClient.openMarket()).willReturn(exchangeResponse);

        // when
        MarketOpenResponse response = marketService.openMarket();

        // then
        assertThat(response.exchangeStatus()).isEqualTo(RUNNING);
        assertThat(response.openedAt()).isEqualTo(openedAt);
    }

    @Test
    void 이미_개장된_상태라면_MARKET_ALREADY_OPEN_예외가_발생한다() {
        // given
        FeignException exception = mock(FeignException.class);
        given(exception.status()).willReturn(409);
        given(exchangeClient.openMarket()).willThrow(exception);

        assertThatThrownBy(() -> marketService.openMarket())
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MARKET_ALREADY_OPEN);
    }
}