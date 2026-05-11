package com.securities.securities_server.domain.stockwallet.service;

import com.securities.securities_server.domain.stock.entity.Stock;
import com.securities.securities_server.domain.stock.repository.StockRepository;
import com.securities.securities_server.domain.stockwallet.controller.request.CreateStockWalletRequest;
import com.securities.securities_server.domain.stockwallet.controller.request.CreditStockWalletRequest;
import com.securities.securities_server.domain.stockwallet.controller.response.StockWalletBalanceResponse;
import com.securities.securities_server.domain.stockwallet.entity.StockWallet;
import com.securities.securities_server.domain.stockwallet.repository.StockWalletRepository;
import com.securities.securities_server.domain.user.entity.User;
import com.securities.securities_server.domain.user.repository.UserRepository;
import com.securities.securities_server.global.exception.CustomException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.securities.securities_server.global.exception.ErrorCode.STOCK_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.STOCK_WALLET_ALREADY_EXISTS;
import static com.securities.securities_server.global.exception.ErrorCode.STOCK_WALLET_NOT_FOUND;
import static com.securities.securities_server.global.exception.ErrorCode.USER_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StockWalletServiceTest {

    @Mock
    StockWalletRepository stockWalletRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    StockRepository stockRepository;

    @InjectMocks
    StockWalletService stockWalletService;

    @Nested
    class 개설_시 {

        @Test
        void 종목_계좌를_개설한다() {
            // given
            Long userId = 1L;
            Long stockId = 1L;
            CreateStockWalletRequest request = new CreateStockWalletRequest(stockId);
            User user = createUser(userId);
            Stock stock = createStock(stockId);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(stockRepository.findById(stockId)).willReturn(Optional.of(stock));
            given(stockWalletRepository.existsByUserIdAndStockId(userId, stockId)).willReturn(false);

            // when
            stockWalletService.createStockWallet(userId, request);

            // then
            verify(stockWalletRepository).save(any(StockWallet.class));
        }

        @Test
        void 이미_개설된_종목_계좌가_존재하면_STOCK_WALLET_ALREADY_EXISTS_예외가_발생한다() {
            // given
            Long userId = 1L;
            Long stockId = 1L;
            CreateStockWalletRequest request = new CreateStockWalletRequest(stockId);
            User user = createUser(userId);
            Stock stock = createStock(stockId);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(stockRepository.findById(stockId)).willReturn(Optional.of(stock));
            given(stockWalletRepository.existsByUserIdAndStockId(userId, stockId)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> stockWalletService.createStockWallet(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(STOCK_WALLET_ALREADY_EXISTS);
        }

        @Test
        void User를_찾을_수_없으면_USER_NOT_FOUND_예외가_발생한다() {
            // given
            Long userId = 1L;
            Long stockId = 1L;
            CreateStockWalletRequest request = new CreateStockWalletRequest(stockId);
            given(userRepository.findById(userId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> stockWalletService.createStockWallet(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(USER_NOT_FOUND);
        }

        @Test
        void Stock을_찾을_수_없으면_STOCK_NOT_FOUND_예외가_발생한다() {
            // given
            Long userId = 1L;
            Long stockId = 1L;
            CreateStockWalletRequest request = new CreateStockWalletRequest(stockId);
            User user = createUser(userId);
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(stockRepository.findById(stockId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> stockWalletService.createStockWallet(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(STOCK_NOT_FOUND);
        }
    }

    @Nested
    class 입고_시 {

        @Test
        void 입고_수량만큼_보유_수량을_증가시키고_현재_종목_계좌_정보를_반환한다() {
            // given
            Long userId = 1L;
            Long stockId = 1L;
            Long stockWalletId = 1L;
            StockWallet stockWallet = createStockWallet(userId, stockId, stockWalletId);
            CreditStockWalletRequest request = new CreditStockWalletRequest(stockWallet.getId(), 10L);
            given(stockWalletRepository.findByIdAndUserId(request.stockWalletId(), userId))
                    .willReturn(Optional.of(stockWallet));

            // when
            StockWalletBalanceResponse response = stockWalletService.creditStockWallet(userId, request);

            // then
            assertThat(response.stockWalletId()).isEqualTo(stockWalletId);
            assertThat(response.stockId()).isEqualTo(stockId);
            assertThat(response.holdingQuantity()).isEqualTo(10L);
            assertThat(response.lockedQuantity()).isEqualTo(0L);
            assertThat(response.availableQuantity()).isEqualTo(10L);
        }

        @Test
        void 종목_계좌를_찾을_수_없으면_STOCK_WALLET_NOT_FOUND_예외가_발생한다() {
            // given
            Long userId = 1L;
            Long stockId = 1L;
            Long stockWalletId = 1L;
            StockWallet stockWallet = createStockWallet(userId, stockId, stockWalletId);
            CreditStockWalletRequest request = new CreditStockWalletRequest(stockWallet.getId(), 10L);
            given(stockWalletRepository.findByIdAndUserId(request.stockWalletId(), userId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> stockWalletService.creditStockWallet(userId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(STOCK_WALLET_NOT_FOUND);
        }
    }

    @Nested
    class 조회_시 {

        @Test
        void 조회된_종목_계좌_정보를_반환한다() {
            // given
            Long userId = 1L;
            Long stockId = 1L;
            Long stockWalletId = 1L;
            StockWallet stockWallet = createStockWallet(userId, stockId, stockWalletId);
            given(stockWalletRepository.findByUserIdAndStockId(userId, stockId)).willReturn(Optional.of(stockWallet));

            // when
            StockWalletBalanceResponse response = stockWalletService.getStockWalletBalance(userId, stockId);

            // then
            assertThat(response.stockWalletId()).isEqualTo(stockWalletId);
            assertThat(response.stockId()).isEqualTo(stockId);
            assertThat(response.holdingQuantity()).isEqualTo(0L);
            assertThat(response.lockedQuantity()).isEqualTo(0L);
            assertThat(response.availableQuantity()).isEqualTo(0L);
        }

        @Test
        void 종목_계좌를_찾을_수_없으면_STOCK_WALLET_NOT_FOUND_예외가_발생한다() {
            // given
            Long userId = 1L;
            Long stockId = 1L;
            Long stockWalletId = 1L;
            given(stockWalletRepository.findByUserIdAndStockId(userId, stockId))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> stockWalletService.getStockWalletBalance(userId, stockId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(STOCK_WALLET_NOT_FOUND);
        }
    }

    private User createUser(Long userId) {
        User user = User.signUp("kiju", "kiju@gmail.com", "Passwo12!@");
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private Stock createStock(Long stockId) {
        Stock stock = new Stock("삼성전자", "001123");
        ReflectionTestUtils.setField(stock, "id", stockId);
        return stock;
    }

    private StockWallet createStockWallet(Long userId, Long stockId, Long stockWalletId) {
        User user = createUser(userId);
        Stock stock = createStock(stockId);
        StockWallet stockWallet = StockWallet.create(user, stock);
        ReflectionTestUtils.setField(stockWallet, "id", stockWalletId);
        return stockWallet;
    }
}