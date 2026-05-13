package com.securities.securities_server.domain.stockwallet.controller;

import com.securities.securities_server.domain.stockwallet.controller.request.CreateStockWalletRequest;
import com.securities.securities_server.domain.stockwallet.controller.request.CreditStockWalletRequest;
import com.securities.securities_server.domain.stockwallet.controller.response.StockWalletBalanceResponse;
import com.securities.securities_server.domain.stockwallet.service.StockWalletService;
import com.securities.securities_server.global.auth.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    public ResponseEntity<StockWalletBalanceResponse> creditStockWallet(
            @AuthUser Long userId,
            @RequestBody CreditStockWalletRequest request
    ) {
        StockWalletBalanceResponse response = stockWalletService.creditStockWallet(userId, request);
        return ResponseEntity.status(OK).body(response);
    }

    @GetMapping("/balance/{stockId}")
    public ResponseEntity<StockWalletBalanceResponse> getBalanceStockWallet(
            @AuthUser Long userId,
            @PathVariable Long stockId
    ) {
        StockWalletBalanceResponse response = stockWalletService.getStockWalletBalance(userId, stockId);
        return ResponseEntity.status(OK).body(response);
    }

    @PostMapping("/{stockWalletId}/block")
    public ResponseEntity<Void> blockStockWallet(@PathVariable Long stockWalletId) {
        stockWalletService.blockStockWallet(stockWalletId);
        return ResponseEntity.status(OK).build();
    }

    @PostMapping("/{stockWalletId}/unblock")
    public ResponseEntity<Void> unblockStockWallet(@PathVariable Long stockWalletId) {
        stockWalletService.unblockStockWallet(stockWalletId);
        return ResponseEntity.status(OK).build();
    }
}
