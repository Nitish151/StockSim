// TransactionRequest.java
package com.example.stockmarketsimulator.modules.transaction.dto;

import com.example.stockmarketsimulator.modules.transaction.model.Transaction.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {
    private String stockSymbol;
    private int quantity;
    private TransactionType type; // BUY or SELL
    private String orderType = "MARKET"; // Default to market order, for future expansion
}