package com.example.stockmarketsimulator.modules.transaction.model;

import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Buyer/Seller

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock; // Which stock was traded

    @Enumerated(EnumType.STRING)
    private TransactionType type; // BUY or SELL

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    private BigDecimal price; // Price at which transaction happened
    private BigDecimal totalPrice = BigDecimal.valueOf(0.0); // Total price of the transaction
    private int quantity; // Number of stocks traded
    private LocalDateTime timestamp = LocalDateTime.now(); // Time of transaction


    public enum TransactionType {
        BUY, SELL;
    }

    public enum OrderType{
        MARKET, LIMIT;
    }

    @Column(name = "profit_or_loss")
    private BigDecimal profitOrLoss;  // This is only relevant for SELL transactions


}


