package com.securities.securities_server.domain.stockwallet.service;

import com.securities.securities_server.domain.stock.entity.Stock;
import com.securities.securities_server.domain.stockwallet.entity.StockWallet;
import com.securities.securities_server.domain.stockwallet.entity.StockWalletHistory;
import com.securities.securities_server.domain.stockwallet.entity.StockWalletTxType;
import com.securities.securities_server.domain.stockwallet.repository.StockWalletHistoryRepository;
import com.securities.securities_server.domain.stockwallet.service.dto.StockWalletHistoryCommand;
import com.securities.securities_server.domain.user.entity.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.BDDMockito;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.securities.securities_server.domain.stockwallet.entity.StockWalletTxType.BUY_EXECUTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StockWalletHistoryServiceTest {

    @Mock
    StockWalletHistoryRepository stockWalletHistoryRepository;

    @InjectMocks
    StockWalletHistoryService stockWalletHistoryService;

    @Test
    void 종목_계좌에_내역을_저장한다() {
        // given
        StockWallet stockWallet = createStockWallet();
        StockWalletHistoryCommand command =
                new StockWalletHistoryCommand(stockWallet, BUY_EXECUTED, 10L, 20L);

        // when
        stockWalletHistoryService.createStockWalletHistory(command);

        // then
        ArgumentCaptor<StockWalletHistory> captor = ArgumentCaptor.forClass(StockWalletHistory.class);
        verify(stockWalletHistoryRepository).save(captor.capture());
        StockWalletHistory savedHistory = captor.getValue();

        assertThat(savedHistory.getStockWallet()).isEqualTo(stockWallet);
        assertThat(savedHistory.getTxType()).isEqualTo(BUY_EXECUTED);
        assertThat(savedHistory.getTxAmount()).isEqualTo(10L);
        assertThat(savedHistory.getRemainingQuantity()).isEqualTo(20L);
    }

    private StockWallet createStockWallet() {
        User user = User.signUp("kiju", "kiju@gmail.com", "Passwer12!@");
        Stock stock = new Stock("삼성전자", "001123");
        return StockWallet.create(user, stock);
    }
}