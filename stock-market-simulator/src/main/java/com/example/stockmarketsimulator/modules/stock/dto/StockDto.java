package com.example.stockmarketsimulator.modules.stock.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor  // 🔥 Required for Jackson
@AllArgsConstructor // 🔥 Ensures the builder works
public class StockDto {
    private String symbol;
    private String shortName;
    private String longName;
    private String exchange;
    private String marketState;

    private BigDecimal regularMarketPrice;
    private BigDecimal regularMarketChange;
    private BigDecimal regularMarketChangePercent;
    private BigDecimal regularMarketPreviousClose;
    private BigDecimal regularMarketOpen;

    private BigDecimal regularMarketDayHigh;
    private BigDecimal regularMarketDayLow;
    private Long regularMarketVolume;
    private BigDecimal marketCap;

    private String fiftyTwoWeekRange;
    private BigDecimal fiftyTwoWeekHigh;
    private BigDecimal fiftyTwoWeekLow;
    private BigDecimal fiftyTwoWeekHighChangePercent;

    private BigDecimal fiftyDayAverage;
    private BigDecimal twoHundredDayAverage;

    private BigDecimal trailingPE;
    private BigDecimal forwardPE;
    private BigDecimal epsTrailingTwelveMonths;
    private BigDecimal dividendYield;
    private BigDecimal priceToBook;

    private LocalDateTime lastUpdated;
}
