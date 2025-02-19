package com.example.stockmarketsimulator.modules.stock.service;

import com.example.stockmarketsimulator.modules.stock.dto.StockDto;

public interface StockDataService {
    /**
     * Fetches the latest stock data for the given symbol.
     * It first checks Redis cache; if not found, it fetches data from the external API and caches it.
     */
    StockDto fetchStockData(String symbol);
}
