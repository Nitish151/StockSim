package com.example.stockmarketsimulator.modules.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockInfo {
    private String symbol;
    private String name;
    private String exch;
    private String type;
    private String exchDisp;
    private String typeDisp;


}
