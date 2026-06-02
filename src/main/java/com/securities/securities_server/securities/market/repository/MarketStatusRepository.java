package com.securities.securities_server.securities.market.repository;

import com.securities.securities_server.securities.market.entity.MarketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface MarketStatusRepository extends JpaRepository<MarketStatus, Long> {
    Optional<MarketStatus> findByStockIdAndTradingDate(Long stockId, LocalDate tradingDate);
}
