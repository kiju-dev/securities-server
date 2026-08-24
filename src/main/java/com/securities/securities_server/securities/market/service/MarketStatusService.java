package com.securities.securities_server.securities.market.service;

import com.securities.securities_server.securities.market.entity.MarketStatus;
import com.securities.securities_server.securities.market.repository.MarketStatusRepository;
import com.securities.securities_server.securities.market.service.dto.MarketPriceInfo;
import com.securities.securities_server.securities.stock.entity.Stock;
import com.securities.securities_server.securities.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketStatusService {

    private final MarketStatusRepository marketStatusRepository;
    private final StockRepository stockRepository;
    private final PriceCalculator priceCalculator;

    @Transactional
    public void createNextDayMarketStatuses() {
        List<Stock> stocks = stockRepository.findAll();
        LocalDate tradingDate = LocalDate.now();
        for (Stock stock : stocks) {
            createNextDayMarketStatus(stock, tradingDate);
        }
    }

    private void createNextDayMarketStatus(Stock stock, LocalDate tradingDate) {
        MarketStatus todayMarketStatus = getTodayMarketStatusOrDefault(stock, tradingDate);

        MarketPriceInfo marketPriceInfo = priceCalculator.calculateMarketPriceInfo(
                        todayMarketStatus.getReferencePrice(),
                        todayMarketStatus.getTradingAmount(),
                        todayMarketStatus.getTradingVolume()
        );
        MarketStatus nextDayMarketStatus = MarketStatus.create(
                stock,
                tradingDate.plusDays(1),
                marketPriceInfo
        );
        marketStatusRepository.save(nextDayMarketStatus);
    }

    private MarketStatus getTodayMarketStatusOrDefault(Stock stock, LocalDate tradingDate) {
        return marketStatusRepository.findByStockIdAndTradingDate(stock.getId(), tradingDate)
                .orElseGet(() -> createDefaultMarketStatus(stock, tradingDate));
    }

    private MarketStatus createDefaultMarketStatus(Stock stock, LocalDate tradingDate) {
        MarketPriceInfo defaultMarketPriceInfo = priceCalculator.getDefaultMarketPriceInfo();
        return MarketStatus.create(
                stock,
                tradingDate,
                defaultMarketPriceInfo
        );
    }
}
