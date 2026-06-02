package com.securities.securities_server.securities.cashwallet.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securities.securities_server.securities.cashwallet.controller.request.DepositCashWalletRequest;
import com.securities.securities_server.securities.cashwallet.controller.request.WithdrawCashWalletRequest;
import com.securities.securities_server.securities.cashwallet.controller.response.CashWalletBalanceResponse;
import com.securities.securities_server.securities.cashwallet.controller.response.CashWalletHistoriesResponse;
import com.securities.securities_server.securities.cashwallet.controller.response.CashWalletHistoryResponse;
import com.securities.securities_server.securities.cashwallet.controller.response.CreateCashWalletResponse;
import com.securities.securities_server.securities.cashwallet.controller.response.DepositCashWalletResponse;
import com.securities.securities_server.securities.cashwallet.controller.response.WithdrawCashWalletResponse;
import com.securities.securities_server.securities.cashwallet.service.CashWalletService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static com.securities.securities_server.securities.cashwallet.entity.CashWalletTxType.DEPOSIT;
import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_SUSPENDED;
import static com.securities.securities_server.global.exception.ErrorCode.INSUFFICIENT_BALANCE;
import static com.securities.securities_server.global.exception.ErrorCode.USER_NOT_FOUND;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    CashWalletService cashWalletService;

    @MockitoBean
    JwtProvider jwtProvider;

    @Nested
    class 개설_시 {

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
        void User를_찾을_수_없으면_예외가_발생한다() throws Exception {
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

    @Nested
    class 입금_시 {

        @Test
        void 입금_후_현금_계좌_잔액을_반환한다() throws Exception {
            // given
            Long userId = 1L;
            DepositCashWalletRequest request = new DepositCashWalletRequest(10000L);
            DepositCashWalletResponse response = new DepositCashWalletResponse(20000L);
            given(cashWalletService.depositCashWallet(userId, request))
                    .willReturn(response);

            // when & then
            mockMvc.perform(post("/api/v1/cash-wallet/deposit")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balanceAfter").value(response.balanceAfter()));
        }

        @Test
        void User를_찾을_수_없으면_예외가_발생한다() throws Exception {
            // given
            Long userId = 1L;
            DepositCashWalletRequest request = new DepositCashWalletRequest(10000L);
            given(cashWalletService.depositCashWallet(userId, request))
                    .willThrow(new CustomException(USER_NOT_FOUND));

            // when & then
            mockMvc.perform(post("/api/v1/cash-wallet/deposit")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("USER_003"));
        }

        @Test
        void 현금_계좌를_찾을_수_없으면_예외가_발생한다() throws Exception {
            // given
            Long userId = 1L;
            DepositCashWalletRequest request = new DepositCashWalletRequest(10000L);
            given(cashWalletService.depositCashWallet(userId, request))
                    .willThrow(new CustomException(CASH_WALLET_NOT_FOUND));

            // when & then
            mockMvc.perform(post("/api/v1/cash-wallet/deposit")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("CASH_003"));
        }

        @Test
        void 현금_계좌가_정지_상태이면_예외가_발생한다() throws Exception {
            // given
            Long userId = 1L;
            DepositCashWalletRequest request = new DepositCashWalletRequest(10000L);
            given(cashWalletService.depositCashWallet(userId, request))
                    .willThrow(new CustomException(CASH_WALLET_SUSPENDED));

            // when & then
            mockMvc.perform(post("/api/v1/cash-wallet/deposit")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("CASH_002"));
        }
    }

    @Nested
    class 출금_시 {

        @Test
        void 출금_후_현금_계좌_잔액을_반환한다() throws Exception {
            // given
            Long userId = 1L;
            WithdrawCashWalletRequest request = new WithdrawCashWalletRequest(10000L);
            WithdrawCashWalletResponse response = new WithdrawCashWalletResponse(20000L);
            given(cashWalletService.withdrawCashWallet(userId, request)).willReturn(response);

            // when & then
            mockMvc.perform(post("/api/v1/cash-wallet/withdraw")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balanceAfter").value(20000L));
        }

        @Test
        void User를_찾을_수_없으면_예외가_발생한다() throws Exception {
            // given
            Long userId = 1L;
            WithdrawCashWalletRequest request = new WithdrawCashWalletRequest(10000L);
            given(cashWalletService.withdrawCashWallet(userId, request))
                    .willThrow(new CustomException(USER_NOT_FOUND));

            // when & then
            mockMvc.perform(post("/api/v1/cash-wallet/withdraw")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("USER_003"));
        }

        @Test
        void 현금_계좌를_찾을_수_없으면_예외가_발생한다() throws Exception {
            // given
            Long userId = 1L;
            WithdrawCashWalletRequest request = new WithdrawCashWalletRequest(10000L);
            given(cashWalletService.withdrawCashWallet(userId, request))
                    .willThrow(new CustomException(CASH_WALLET_NOT_FOUND));

            // when & then
            mockMvc.perform(post("/api/v1/cash-wallet/withdraw")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("CASH_003"));
        }

        @Test
        void 현금_계좌가_정지_상태이면_예외가_발생한다() throws Exception {
            // given
            Long userId = 1L;
            WithdrawCashWalletRequest request = new WithdrawCashWalletRequest(10000L);
            given(cashWalletService.withdrawCashWallet(userId, request))
                    .willThrow(new CustomException(CASH_WALLET_SUSPENDED));

            // when & then
            mockMvc.perform(post("/api/v1/cash-wallet/withdraw")
                            .contentType(APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.code").value("CASH_002"));
        }

        @Test
        void 출금_금액이_사용_가능한_잔액보다_크면_예외가_발생한다() throws Exception {
            // given
            Long userId = 1L;
            WithdrawCashWalletRequest request = new WithdrawCashWalletRequest(10000L);
            given(cashWalletService.withdrawCashWallet(userId, request))
                    .willThrow(new CustomException(INSUFFICIENT_BALANCE));

            // when & then
            mockMvc.perform(post("/api/v1/cash-wallet/withdraw")
                    .contentType(APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("CASH_007"));
        }

    }

    @Nested
    class 잔액_조회_시 {

        @Test
        void 잔액과_매수주문으로_묶인_금액_사용_가능한_잔액을_반환한다() throws Exception {
            // given
            Long userId = 1L;
            CashWalletBalanceResponse response =
                    new CashWalletBalanceResponse(30000L, 10000L, 20000L);
            given(cashWalletService.getBalance(userId)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/v1/cash-wallet/balance"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(30000L))
                    .andExpect(jsonPath("$.lockedAmount").value(10000L))
                    .andExpect(jsonPath("$.availableAmount").value(20000L));
            verify(cashWalletService).getBalance(userId);
        }

        @Test
        void User를_찾을_수_없으면_예외가_발생한다() throws Exception {
            // given
            Long userId = 1L;
            given(cashWalletService.getBalance(userId))
                    .willThrow(new CustomException(USER_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/v1/cash-wallet/balance"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("USER_003"));
        }

        @Test
        void 현금_계좌를_찾을_수_없으면_예외가_발생한다() throws Exception {
            // given
            Long userId = 1L;
            given(cashWalletService.getBalance(userId))
                    .willThrow(new CustomException(CASH_WALLET_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/v1/cash-wallet/balance"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("CASH_003"));
        }
    }

    @Nested
    class 내역_조회_시 {

        @Test
        void 현금_계좌_내역을_페이징하여_반환한다() throws Exception {
            // given
            Long userId = 1L;
            Pageable pageable = PageRequest.of(0, 10);
            CashWalletHistoryResponse history1 =
                    new CashWalletHistoryResponse(
                            1L,
                            DEPOSIT,
                            20000L,
                            30000L,
                            LocalDateTime.now()
                    );
            CashWalletHistoryResponse history2 =
                    new CashWalletHistoryResponse(
                            2L,
                            DEPOSIT,
                            20000L,
                            50000L,
                            LocalDateTime.now()
                    );
            CashWalletHistoriesResponse response = new CashWalletHistoriesResponse(
                    2L,
                    List.of(history1, history2)
            );
            given(cashWalletService.getHistories(userId, pageable)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/v1/cash-wallet/histories")
                            .queryParam("page", String.valueOf(0))
                            .queryParam("size", String.valueOf(10)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.cashWalletHistories[0].historyId").value(history1.historyId()))
                    .andExpect(jsonPath("$.cashWalletHistories[1].historyId").value(history2.historyId()));
        }
    }

    @Nested
    class 계좌_정지_시 {

        @Test
        void 현금_계좌를_정지시키고_200_OK를_반환한다() throws Exception {
            // given
            Long cashWalletId = 1L;

            // when & then
            mockMvc.perform(post("/api/v1/cash-wallet/{cashWalletId}/block", cashWalletId))
                    .andExpect(status().isOk());
            verify(cashWalletService).blockCashWallet(cashWalletId);
        }
    }

    @Nested
    class 계좌_정지_해제_시 {

        @Test
        void 현금_계좌_정지를_해제시키고_200_OK를_반환한다() throws Exception {
            // given
            Long cashWalletId = 1L;

            // when & then
            mockMvc.perform(post("/api/v1/cash-wallet/{cashWalletId}/unblock", cashWalletId))
                    .andExpect(status().isOk());
            verify(cashWalletService).unblockCashWallet(cashWalletId);
        }
    }
}