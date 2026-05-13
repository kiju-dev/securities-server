package com.securities.securities_server.domain.market.repository;

import com.securities.securities_server.domain.market.entity.MarketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketStatusRepository extends JpaRepository<MarketStatus, Long> {
}
