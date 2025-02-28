package com.example.stockmarketsimulator.modules.transaction.dto;

import com.example.stockmarketsimulator.modules.transaction.model.Transaction.TransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private String stockSymbol; // Only the stock symbol
    private String companyName; // Only the company name
    private TransactionType type;
    private BigDecimal price;
    private BigDecimal totalPrice;
    private int quantity;
    private LocalDateTime timestamp;
}