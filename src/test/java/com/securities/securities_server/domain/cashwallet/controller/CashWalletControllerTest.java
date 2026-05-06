package com.securities.securities_server.domain.cashwallet.controller;

import com.securities.securities_server.domain.cashwallet.controller.response.CreateCashWalletResponse;
import com.securities.securities_server.domain.cashwallet.service.CashWalletService;
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

import static com.securities.securities_server.global.exception.ErrorCode.USER_NOT_FOUND;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CashWalletController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = WebConfig.class
        )
)
@Import(TestWebConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CashWalletControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CashWalletService cashWalletService;

    @MockitoBean
    JwtProvider jwtProvider;

    @Test
    void 현금_계좌를_개설한다() throws Exception {
        // given
        Long userId = 1L;
        String accountNumber = "777123456781";

        given(cashWalletService.createCashWallet(userId))
                .willReturn(new CreateCashWalletResponse(accountNumber));

        // when & then
        mockMvc.perform(post("/api/v1/cash-wallet"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber));

        verify(cashWalletService).createCashWallet(userId);
    }

    @Test
    void 계좌_생성_시_사용자_정보가_없으면_예외가_발생한다() throws Exception {
        // given
        Long userId = 1L;
        given(cashWalletService.createCashWallet(userId))
                .willThrow(new CustomException(USER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/v1/cash-wallet"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_003"));
    }
}