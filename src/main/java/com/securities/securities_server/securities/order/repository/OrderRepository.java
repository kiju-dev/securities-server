package com.securities.securities_server.securities.order.repository;

import com.securities.securities_server.securities.order.entity.Order;
import com.securities.securities_server.global.common.OrderSide;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserIdAndStockIdAndSideAndUnfilledQuantityGreaterThan(Long userId, Long StockId, OrderSide side, long quantity, Pageable pageable);
}
