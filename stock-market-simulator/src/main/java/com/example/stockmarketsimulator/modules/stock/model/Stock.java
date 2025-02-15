package com.example.stockmarketsimulator.modules.stock.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Stock symbol cannot be blank")
    @Column(unique = true, nullable = false, updatable = false)
    private String symbol;  // Example: AAPL, TSLA

    @NotBlank(message = "Company name cannot be blank")
    private String companyName;  // Example: Apple Inc.

    @NotBlank(message = "Industry cannot be blank")
    private String industry;  // Example: Technology, Finance

    @NotNull(message = "Current price cannot be null")
    private BigDecimal currentPrice;  // Example: 150.75

    private BigDecimal openingPrice;   // Price at market open
    private BigDecimal previousClose;  // Last trading day's close

    private Long volume;  // Number of shares traded
    private BigDecimal marketCap; // Market capitalization

    private BigDecimal priceChange;  // Price difference from previous close
    private BigDecimal percentageChange;  // Percentage change from previous close

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdated = LocalDateTime.now();  // Last market update
}
