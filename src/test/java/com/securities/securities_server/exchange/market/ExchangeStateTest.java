package com.securities.securities_server.exchange.market;

import com.securities.securities_server.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static com.securities.securities_server.global.exception.ErrorCode.MARKET_ALREADY_CLOSE;
import static com.securities.securities_server.global.exception.ErrorCode.MARKET_ALREADY_OPEN;
import static com.securities.securities_server.securities.market.entity.ExchangeStatus.RUNNING;
import static com.securities.securities_server.securities.market.entity.ExchangeStatus.STOPPED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ExchangeStateTest {
    private ExchangeState exchangeState;

    @BeforeEach
    void setUp() {
        exchangeState = new ExchangeState();
    }

    @Test
    void 초기_상태는_STOPPED이다() {
        assertThat(exchangeState.getStatus()).isEqualTo(STOPPED);
        assertThat(exchangeState.isRunning()).isFalse();
    }

    @Test
    void 장을_열면_RUNNING_상태가_된다() {
        exchangeState.open();
        assertThat(exchangeState.getStatus()).isEqualTo(RUNNING);
        assertThat(exchangeState.isRunning()).isTrue();
    }

    @Test
    void 이미_RUNNING이면_다시_열_수_없다() {
        exchangeState.open();
        assertThatThrownBy(() -> exchangeState.open())
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MARKET_ALREADY_OPEN);
    }

    @Test
    void 장을_닫으면_STOPPED_상태가_된다() {
        exchangeState.open();
        exchangeState.close();
        assertThat(exchangeState.getStatus()).isEqualTo(STOPPED);
        assertThat(exchangeState.isRunning()).isFalse();
    }

    @Test
    void 이미_STOPPED이면_다시_닫을_수_없다() {
        assertThatThrownBy(() -> exchangeState.close())
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(MARKET_ALREADY_CLOSE);
    }
}