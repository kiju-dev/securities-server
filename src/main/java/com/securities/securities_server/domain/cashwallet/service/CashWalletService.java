package com.securities.securities_server.domain.cashwallet.service;

import com.securities.securities_server.domain.cashwallet.controller.response.CreateCashWalletResponse;
import com.securities.securities_server.domain.cashwallet.entity.CashWallet;
import com.securities.securities_server.domain.cashwallet.repository.CashWalletRepository;
import com.securities.securities_server.domain.user.entity.User;
import com.securities.securities_server.domain.user.repository.UserRepository;
import com.securities.securities_server.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.securities.securities_server.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CashWalletService {

    private final CashWalletRepository cashWalletRepository;
    private final UserRepository userRepository;
    private final AccountNumberGenerator accountNumberGenerator;

    @Transactional
    public CreateCashWalletResponse createCashWallet(Long userId) {
        User user = getUser(userId);
        String accountNumber = accountNumberGenerator.generateAccountNumber();

        CashWallet cashWallet = CashWallet.create(user, accountNumber);
        cashWalletRepository.save(cashWallet);
        return new CreateCashWalletResponse(accountNumber);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }
}
