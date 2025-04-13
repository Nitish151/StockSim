package com.example.stockmarketsimulator.modules.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsDto {
    private String type;
    private String url;
    private String img;
    private String text;
    private String time;
    private String ago;
    private String source;
    private String title;
    private String tickers[];
}
