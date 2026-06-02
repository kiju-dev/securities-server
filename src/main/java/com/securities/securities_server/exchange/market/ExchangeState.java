package com.securities.securities_server.exchange.market;

import com.securities.securities_server.global.exception.CustomException;
import com.securities.securities_server.securities.market.entity.ExchangeStatus;
import lombok.Getter;
import org.springframework.stereotype.Component;

import static com.securities.securities_server.global.exception.ErrorCode.MARKET_ALREADY_CLOSE;
import static com.securities.securities_server.global.exception.ErrorCode.MARKET_ALREADY_OPEN;
import static com.securities.securities_server.global.exception.ErrorCode.MARKET_NOT_OPEN;
import static com.securities.securities_server.securities.market.entity.ExchangeStatus.RUNNING;
import static com.securities.securities_server.securities.market.entity.ExchangeStatus.STOPPED;

@Component
@Getter
public class ExchangeState {

    private ExchangeStatus status = STOPPED;

    public boolean isRunning() {
        return status == RUNNING;
    }

    public void open() {
        if (status == RUNNING) {
            throw new CustomException(MARKET_ALREADY_OPEN);
        }
        status = RUNNING;
    }

    public void close() {
        if (status == STOPPED) {
            throw new CustomException(MARKET_ALREADY_CLOSE);
        }
        status = STOPPED;
    }

    public void validateRunning() {
        if (status != RUNNING) {
            throw new CustomException(MARKET_NOT_OPEN);
        }
    }
}
