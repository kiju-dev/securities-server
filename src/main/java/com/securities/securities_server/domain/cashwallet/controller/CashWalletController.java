package com.securities.securities_server.domain.cashwallet.controller;

import com.securities.securities_server.domain.cashwallet.controller.response.CreateCashWalletResponse;
import com.securities.securities_server.domain.cashwallet.service.CashWalletService;
import com.securities.securities_server.global.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;

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
}
