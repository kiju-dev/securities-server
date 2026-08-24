package com.securities.securities_server.securities.market.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securities.securities_server.exchange.market.dto.response.MarketCloseResponse;
import com.securities.securities_server.exchange.market.dto.response.MarketOpenResponse;
import com.securities.securities_server.securities.market.service.MarketService;
import com.securities.securities_server.global.auth.JwtProvider;
import com.securities.securities_server.global.config.WebConfig;
import com.securities.securities_server.global.exception.CustomException;
import com.securities.securities_server.support.TestWebConfig;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.securities.securities_server.global.common.ExchangeStatus.RUNNING;
import static com.securities.securities_server.global.common.ExchangeStatus.STOPPED;
import static com.securities.securities_server.global.exception.ErrorCode.MARKET_ALREADY_CLOSE;
import static com.securities.securities_server.global.exception.ErrorCode.MARKET_ALREADY_OPEN;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MarketController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = WebConfig.class
        )
)
@Import(TestWebConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class MarketControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    MarketService marketService;

    @MockitoBean
    JwtProvider jwtProvider;

    @Test
    void 거래소_서버에_장_시작을_요청하고_서버_상태를_반환받는다() throws Exception {
        // given
        LocalDateTime openedAt = LocalDateTime.of(2026, 5, 13, 9, 0);
        MarketOpenResponse response = new MarketOpenResponse(RUNNING, openedAt);
        given(marketService.openMarket()).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/market/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exchangeStatus").value("RUNNING"))
                .andExpect(jsonPath("$.openedAt").value("2026-05-13T09:00:00"));
    }

    @Test
    void 이미_개장된_상태인_경우에는_에러가_발생한다() throws Exception {
        // given
        given(marketService.openMarket()).willThrow(new CustomException(MARKET_ALREADY_OPEN));

        // when & then
        mockMvc.perform(post("/api/v1/market/open"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MARKET_001"));
    }

    @Test
    void 거래소_서버에_장_종료를_요청하고_서버_상태를_반환받는다() throws Exception {
        // given
        LocalDateTime closedAt = LocalDateTime.of(2026, 5, 13, 15, 30);
        MarketCloseResponse response = new MarketCloseResponse(STOPPED, closedAt);
        given(marketService.closeMarket()).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/market/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exchangeStatus").value("STOPPED"))
                .andExpect(jsonPath("$.closedAt").value("2026-05-13T15:30:00"));
    }

    @Test
    void 이미_폐장된_상태인_경우에는_에러가_발생한다() throws Exception {
        // given
        given(marketService.closeMarket()).willThrow(new CustomException(MARKET_ALREADY_CLOSE));

        // when & then
        mockMvc.perform(post("/api/v1/market/close"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("MARKET_002"));
    }
}