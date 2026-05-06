package com.securities.securities_server.domain.cashwallet.service;

import com.securities.securities_server.domain.cashwallet.controller.request.DepositCashWalletRequest;
import com.securities.securities_server.domain.cashwallet.controller.response.CreateCashWalletResponse;
import com.securities.securities_server.domain.cashwallet.controller.response.DepositCashWalletResponse;
import com.securities.securities_server.domain.cashwallet.entity.CashWallet;
import com.securities.securities_server.domain.cashwallet.entity.CashWalletTxType;
import com.securities.securities_server.domain.cashwallet.repository.CashWalletRepository;
import com.securities.securities_server.domain.cashwallet.service.dto.CashWalletHistoryCommand;
import com.securities.securities_server.domain.user.entity.User;
import com.securities.securities_server.domain.user.repository.UserRepository;
import com.securities.securities_server.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.securities.securities_server.domain.cashwallet.entity.CashWalletTxType.DEPOSIT;
import static com.securities.securities_server.global.exception.ErrorCode.CASH_WALLET_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CashWalletService {

    private final CashWalletHistoryService cashWalletHistoryService;

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

    @Transactional
    public DepositCashWalletResponse depositCashWallet(Long userId, DepositCashWalletRequest request) {
        User user = getUser(userId);
        CashWallet cashWallet = getCashWallet(user);

        cashWallet.deposit(request.amount());
        saveHistory(cashWallet, DEPOSIT, request.amount());
        return new DepositCashWalletResponse(cashWallet.getBalance());
    }

    private void saveHistory(CashWallet cashWallet, CashWalletTxType txType, long amount) {
        CashWalletHistoryCommand command =
                new CashWalletHistoryCommand(
                        cashWallet,
                        txType,
                        amount,
                        cashWallet.getBalance()
                );
        cashWalletHistoryService.createCashWalletHistory(command);
    }

    private CashWallet getCashWallet(User user) {
        return cashWalletRepository.findByUser(user)
                .orElseThrow(() -> new CustomException(CASH_WALLET_NOT_FOUND));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
    }
}
