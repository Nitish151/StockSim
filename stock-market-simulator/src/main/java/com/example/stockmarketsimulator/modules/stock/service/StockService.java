package com.example.stockmarketsimulator.modules.stock.service;

import com.example.stockmarketsimulator.modules.stock.dto.NewsDto;
import com.example.stockmarketsimulator.modules.stock.dto.NewsResponseDto;
import com.example.stockmarketsimulator.modules.stock.dto.SearchResponseDto;
import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.example.stockmarketsimulator.modules.stock.model.Stock;

public interface StockService {
    /**
     * Fetches stock data for the given symbol.
     * Uses cache if available, otherwise fetches from the external API.
     */
    StockDto getStockData(String symbol);

    /**
     * Fetches stock data and ensures it's persisted in the database.
     * Returns the persisted entity.
     */
    Stock getAndPersistStock(String symbol);
    SearchResponseDto searchStocksByName(String stockName);

    NewsResponseDto getNews(String tickers, String type);
}