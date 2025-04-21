package com.example.stockmarketsimulator.modules.transaction.repository;

import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.transaction.model.LimitOrder;
import com.example.stockmarketsimulator.modules.transaction.model.Transaction;
import com.example.stockmarketsimulator.modules.transaction.service.LimitOrderService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LimitOrderRepository extends JpaRepository<LimitOrder, Long> {

    List<LimitOrder> findByUserId(Long userId);

    List<LimitOrder> findByUserIdAndStatus(Long userId, LimitOrder.LimitOrderStatus status);

    List<LimitOrder> findByStatus(LimitOrder.LimitOrderStatus status);

    List<LimitOrder> findByStockIdAndStatus(Long stockId, LimitOrder.LimitOrderStatus status);

    // Find buy limit orders that should be executed (current price <= limit price)
    @Query("SELECT lo FROM LimitOrder lo WHERE lo.stock.id = :stockId AND lo.type = 'BUY' " +
            "AND lo.status = 'PENDING' AND lo.limitPrice >= :currentPrice")
    List<LimitOrder> findBuyOrdersToExecute(Long stockId, BigDecimal currentPrice);

    // Find sell limit orders that should be executed (current price >= limit price)
    @Query("SELECT lo FROM LimitOrder lo WHERE lo.stock.id = :stockId AND lo.type = 'SELL' " +
            "AND lo.status = 'PENDING' AND lo.limitPrice <= :currentPrice")
    List<LimitOrder> findSellOrdersToExecute(Long stockId, BigDecimal currentPrice);

    // Find expired limit orders
    List<LimitOrder> findByStatusAndExpiresAtBefore(LimitOrder.LimitOrderStatus status, LocalDateTime now);
}