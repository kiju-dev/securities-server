package com.securities.securities_server.securities.cashwallet.service;

import com.securities.securities_server.securities.cashwallet.controller.request.DepositCashWalletRequest;
import com.securities.securities_server.securities.cashwallet.controller.request.WithdrawCashWalletRequest;
import com.securities.securities_server.securities.cashwallet.entity.CashWallet;
import com.securities.securities_server.securities.cashwallet.repository.CashWalletRepository;
import com.securities.securities_server.securities.stock.entity.Stock;
import com.securities.securities_server.securities.stock.repository.StockRepository;
import com.securities.securities_server.securities.stockwallet.controller.request.CreditStockWalletRequest;
import com.securities.securities_server.securities.stockwallet.entity.StockWallet;
import com.securities.securities_server.securities.stockwallet.repository.StockWalletRepository;
import com.securities.securities_server.securities.stockwallet.service.StockWalletService;
import com.securities.securities_server.securities.user.entity.User;
import com.securities.securities_server.securities.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:wallet-concurrency;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000")
class WalletConcurrencyTest {

    private static final AtomicLong SEQ = new AtomicLong();

    @Autowired
    private CashWalletService cashWalletService;
    @Autowired
    private StockWalletService stockWalletService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CashWalletRepository cashWalletRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private StockWalletRepository stockWalletRepository;

    @Test
    void 잔고_10000원에_1000원_출금_20건이_동시에_들어오면_10건만_성공하고_잔고는_0원이다() throws Exception {
        // given
        User user = saveUser();
        saveCashWallet(user);
        cashWalletService.depositCashWallet(user.getId(), new DepositCashWalletRequest(10_000L));

        // when
        AtomicInteger success = new AtomicInteger();
        runConcurrently(20, i -> {
            cashWalletService.withdrawCashWallet(user.getId(), new WithdrawCashWalletRequest(1_000L));
            success.incrementAndGet();
        });

        // then
        CashWallet cashWallet = cashWalletRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(success.get()).isEqualTo(10);
        assertThat(cashWallet.getBalance()).isZero();
    }

    @Test
    void 입금_50건이_동시에_들어와도_모두_잔고에_반영된다() throws Exception {
        // given
        User user = saveUser();
        saveCashWallet(user);

        // when
        runConcurrently(50, i ->
                cashWalletService.depositCashWallet(user.getId(), new DepositCashWalletRequest(100L))
        );

        // then
        CashWallet cashWallet = cashWalletRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(cashWallet.getBalance()).isEqualTo(50 * 100L);
    }

    @Test
    void 주식_입고_50건이_동시에_들어와도_모두_보유_수량에_반영된다() throws Exception {
        // given
        User user = saveUser();
        long seq = SEQ.incrementAndGet();
        Stock stock = stockRepository.save(new Stock("동시성종목" + seq, String.format("%06d", 900_000 + seq)));
        StockWallet stockWallet = stockWalletRepository.save(StockWallet.create(user, stock));

        // when
        runConcurrently(50, i ->
                stockWalletService.creditStockWallet(user.getId(), new CreditStockWalletRequest(stockWallet.getId(), 1L))
        );

        // then
        StockWallet result = stockWalletRepository.findById(stockWallet.getId()).orElseThrow();
        assertThat(result.getHoldingQuantity()).isEqualTo(50L);
    }

    private User saveUser() {
        long seq = SEQ.incrementAndGet();
        return userRepository.save(User.signUp("동시성" + seq, "concurrency" + seq + "@test.com", "password"));
    }

    private void saveCashWallet(User user) {
        cashWalletRepository.save(CashWallet.create(user, "CC-" + System.nanoTime()));
    }

    private void runConcurrently(int taskCount, ThrowingIntConsumer task) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(taskCount);
        CountDownLatch ready = new CountDownLatch(taskCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(taskCount);

        try {
            for (int i = 0; i < taskCount; i++) {
                long index = i;
                executor.submit(() -> {
                    ready.countDown();
                    try {
                        start.await();
                        task.accept(index);
                    } catch (Exception ignored) {
                    } finally {
                        done.countDown();
                    }
                });
            }

            ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        } finally {
            executor.shutdownNow();
        }
    }

    @FunctionalInterface
    private interface ThrowingIntConsumer {
        void accept(long index) throws Exception;
    }
}
