package com.example.stockmarketsimulator.modules.portfolio.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PortfolioSummaryDto {
    private BigDecimal totalInvested;      // Sum of all avgBuyPrice * quantity
    private BigDecimal currentValue;       // Sum of all currentPrice * quantity
    private BigDecimal totalProfitOrLoss;  // currentValue - totalInvested
    private BigDecimal availableBalance;   // Remaining balance user has
    private int totalStocksHeld;           // Total different stocks held
    private List<PortfolioStockDto> holdings; // The detailed list

}
