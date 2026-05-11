package com.securities.securities_server.domain.stockwallet.controller;

import com.securities.securities_server.domain.stockwallet.controller.request.CreateStockWalletRequest;
import com.securities.securities_server.domain.stockwallet.controller.request.CreditStockWalletRequest;
import com.securities.securities_server.domain.stockwallet.controller.response.StockWalletQuantityResponse;
import com.securities.securities_server.domain.stockwallet.service.StockWalletService;
import com.securities.securities_server.global.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stock-wallet")
public class StockWalletController {

    private final StockWalletService stockWalletService;

    @PostMapping
    public ResponseEntity<Void> createStockWallet(
            @AuthUser Long userId,
            @RequestBody CreateStockWalletRequest request
    ) {
        stockWalletService.createStockWallet(userId, request);
        return ResponseEntity.status(CREATED).build();
    }

    @PostMapping("/credit")
    public ResponseEntity<StockWalletQuantityResponse> creditStockWallet(
            @AuthUser Long userId,
            @RequestBody CreditStockWalletRequest request
    ) {
        StockWalletQuantityResponse response = stockWalletService.creditStockWallet(userId, request);
        return ResponseEntity.status(OK).body(response);
    }
}
