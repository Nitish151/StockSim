package com.example.stockmarketsimulator.modules.transaction.model;

import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "limit_orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LimitOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    private Transaction.TransactionType type; // BUY or SELL

    private BigDecimal limitPrice;
    private int quantity;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private LimitOrderStatus status = LimitOrderStatus.PENDING;

    public enum LimitOrderStatus {
        PENDING, EXECUTED, EXPIRED, CANCELED
    }
}