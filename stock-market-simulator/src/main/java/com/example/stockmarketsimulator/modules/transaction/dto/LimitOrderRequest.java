package com.example.stockmarketsimulator.modules.transaction.dto;

import com.example.stockmarketsimulator.modules.transaction.model.LimitOrder;
import com.example.stockmarketsimulator.modules.transaction.model.Transaction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LimitOrderRequest {
    private String stockSymbol;
    private int quantity;
    private Transaction.TransactionType type;
    private BigDecimal limitPrice;
    private LocalDateTime expiresAt;
}