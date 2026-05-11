package com.securities.securities_server.domain.stockwallet.service;

import com.securities.securities_server.domain.stock.entity.Stock;
import com.securities.securities_server.domain.stock.repository.StockRepository;
import com.securities.securities_server.domain.stockwallet.controller.request.CreateStockWalletRequest;
import com.securities.securities_server.domain.stockwallet.controller.request.CreditStockWalletRequest;
import com.securities.securities_server.domain.stockwallet.controller.response.StockWalletQuantityResponse;
import com.securities.securities_server.domain.stockwallet.entity.StockWallet;
import com.securities.securities_server.domain.stockwallet.repository.StockWalletRepository;
import com.securities.securities_server.domain.user.entity.User;
import com.securities.securities_server.domain.user.repository.UserRepository;
import com.securities.securities_server.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.securities.securities_server.global.exception.ErrorCode.STOCK_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.STOCK_WALLET_ALREADY_EXISTS;
import static com.securities.securities_server.global.exception.ErrorCode.STOCK_WALLET_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class StockWalletService {

    private final StockWalletRepository stockWalletRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    @Transactional
    public void createStockWallet(Long userId, CreateStockWalletRequest request) {
        User user = getUser(userId);
        Stock stock = getStock(request.stockId());
        validateDuplicateStockWallet(user.getId(), stock.getId());

        StockWallet stockWallet = StockWallet.create(user, stock);
        stockWalletRepository.save(stockWallet);
    }

    @Transactional
    public StockWalletQuantityResponse creditStockWallet(Long userId, CreditStockWalletRequest request) {
        StockWallet stockWallet = getStockWallet(request.stockWalletId(), userId);
        stockWallet.credit(request.quantity());
        return new StockWalletQuantityResponse(stockWallet.getHoldingQuantity());
    }

    private void validateDuplicateStockWallet(Long userId, Long stockId) {
        if (stockWalletRepository.existsByUserIdAndStockId(userId, stockId)) {
            throw new CustomException(STOCK_WALLET_ALREADY_EXISTS);
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }

    private Stock getStock(Long stockId) {
        return stockRepository.findById(stockId)
                .orElseThrow(() -> new CustomException(STOCK_NOT_FOUND));
    }

    private StockWallet getStockWallet(Long stockWalletId, Long userId) {
        return stockWalletRepository.findByIdAndUserId(stockWalletId, userId)
                .orElseThrow(() -> new CustomException(STOCK_WALLET_NOT_FOUND));
    }
}
