package com.example.stockmarketsimulator.modules.portfolio.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioStockDto {
    private Long id;
    private String stockSymbol; // Only the stock symbol
    private String companyName; // Only the company name
    private BigDecimal currentPrice; // Current price of the stock
    private int quantity; // Number of shares held
    private BigDecimal avgBuyPrice; // Average buy price
    private BigDecimal totalInvestment; // Total investment (avgBuyPrice * quantity)
    private BigDecimal currentValue; // Current value of the holding (currentPrice * quantity)
    private BigDecimal profitOrLoss; // Profit or loss (currentValue - totalInvestment)
}