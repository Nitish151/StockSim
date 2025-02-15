package com.example.stockmarketsimulator.modules.stock.service;

import com.example.stockmarketsimulator.modules.stock.model.Stock;

public interface StockDataService {
    /**
     * Fetches the latest stock data for the given symbol.
     * It first checks Redis cache; if not found, it fetches data from the external API and caches it.
     */
    Stock fetchStockData(String symbol);
}
