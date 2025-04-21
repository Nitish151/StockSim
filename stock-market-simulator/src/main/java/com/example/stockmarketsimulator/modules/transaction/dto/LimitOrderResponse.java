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
public class LimitOrderResponse {
    private Long id;
    private String stockSymbol;
    private String companyName;
    private Transaction.TransactionType type;
    private BigDecimal limitPrice;
//    private String orderType;
    private int quantity;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}