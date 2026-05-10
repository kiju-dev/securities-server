package com.securities.securities_server.domain.stockwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securities.securities_server.domain.stockwallet.controller.request.CreateStockWalletRequest;
import com.securities.securities_server.domain.stockwallet.service.StockWalletService;
import com.securities.securities_server.global.auth.JwtProvider;
import com.securities.securities_server.global.config.WebConfig;
import com.securities.securities_server.global.exception.CustomException;
import com.securities.securities_server.support.TestWebConfig;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.securities.securities_server.global.exception.ErrorCode.STOCK_WALLET_ALREADY_EXISTS;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = StockWalletController.class,
        excludeFilters = {@ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = WebConfig.class
        )}
)
@Import(TestWebConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StockWalletControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    JwtProvider jwtProvider;

    @MockitoBean
    StockWalletService stockWalletService;

    @Nested
    class 개설_시 {

        @Test
        void 종목_계좌를_개설한다() throws Exception {
            // given
            Long userId = 1L;
            Long stockId = 1L;
            CreateStockWalletRequest request = new CreateStockWalletRequest(stockId);

            // when & then
            mockMvc.perform(post("/api/v1/stock-wallet")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
            verify(stockWalletService).createStockWallet(userId, request);
        }

        @Test
        void 이미_개설된_종목_계좌가_존재하면_예외가_발생한다() throws Exception {
            // given
            Long userId = 1L;
            Long stockId = 1L;
            CreateStockWalletRequest request = new CreateStockWalletRequest(stockId);
            willThrow(new CustomException(STOCK_WALLET_ALREADY_EXISTS))
                    .given(stockWalletService)
                    .createStockWallet(userId, request);

            // when & then
            mockMvc.perform(post("/api/v1/stock-wallet")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("STOCK_WALLET_001"));
        }
    }
}