package com.securities.securities_server.domain.cashwallet.controller;

import com.securities.securities_server.domain.cashwallet.controller.request.DepositCashWalletRequest;
import com.securities.securities_server.domain.cashwallet.controller.request.WithdrawCashWalletRequest;
import com.securities.securities_server.domain.cashwallet.controller.response.CashWalletBalanceResponse;
import com.securities.securities_server.domain.cashwallet.controller.response.CashWalletHistoriesResponse;
import com.securities.securities_server.domain.cashwallet.controller.response.CreateCashWalletResponse;
import com.securities.securities_server.domain.cashwallet.controller.response.DepositCashWalletResponse;
import com.securities.securities_server.domain.cashwallet.controller.response.WithdrawCashWalletResponse;
import com.securities.securities_server.domain.cashwallet.service.CashWalletService;
import com.securities.securities_server.global.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cash-wallet")
public class CashWalletController {

    private final CashWalletService cashWalletService;

    @PostMapping
    public ResponseEntity<CreateCashWalletResponse> createCashWallet(@AuthUser Long userId) {
        CreateCashWalletResponse response = cashWalletService.createCashWallet(userId);
        return ResponseEntity.status(CREATED).body(response);
    }

    @PostMapping("/deposit")
    public ResponseEntity<DepositCashWalletResponse> depositCashWallet(
            @AuthUser Long userId,
            @RequestBody DepositCashWalletRequest request
    ) {
        DepositCashWalletResponse response = cashWalletService.depositCashWallet(userId, request);
        return ResponseEntity.status(OK).body(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<WithdrawCashWalletResponse> withdrawCashWallet(
            @AuthUser Long userId,
            @RequestBody WithdrawCashWalletRequest request
    ) {
        WithdrawCashWalletResponse response = cashWalletService.withdrawCashWallet(userId, request);
        return ResponseEntity.status(OK).body(response);
    }

    @GetMapping("/balance")
    public ResponseEntity<CashWalletBalanceResponse> getBalance(@AuthUser Long userId) {
        CashWalletBalanceResponse response = cashWalletService.getBalance(userId);
        return ResponseEntity.status(OK).body(response);
    }

    @GetMapping("/histories")
    public ResponseEntity<CashWalletHistoriesResponse> getHistories(
            @AuthUser Long userId,
            Pageable pageable
    ) {
        CashWalletHistoriesResponse response = cashWalletService.getHistories(userId, pageable);
        return ResponseEntity.status(OK).body(response);
    }
}
