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
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Stock symbol cannot be blank")
    @Column(unique = true, nullable = false, updatable = false)
    private String symbol;

    @NotBlank(message = "Company name cannot be blank")
    private String companyName;

    @NotBlank(message = "Industry cannot be blank")
    private String industry;

    @NotNull(message = "Current price cannot be null")
    private BigDecimal currentPrice;

    private BigDecimal openingPrice;
    private BigDecimal previousClose;

    private Long volume;
    private BigDecimal marketCap;

    private BigDecimal priceChange;
    private BigDecimal percentageChange;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdated = LocalDateTime.now();
}