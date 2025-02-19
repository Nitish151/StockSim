package com.example.stockmarketsimulator.modules.stock.service;

import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class StockStorageService {
    private final StockRepository stockRepository;

    public Stock saveOrUpdateStock(Stock stock) {
        return stockRepository.findBySymbol(stock.getSymbol())
                .map(existingStock -> updateStock(existingStock, stock))
                .orElseGet(() -> stockRepository.save(stock));
    }

    private Stock updateStock(Stock existingStock, Stock newStock) {
        existingStock.setCurrentPrice(newStock.getCurrentPrice());
        existingStock.setOpeningPrice(newStock.getOpeningPrice());
        existingStock.setPreviousClose(newStock.getPreviousClose());
        existingStock.setVolume(newStock.getVolume());
        existingStock.setMarketCap(newStock.getMarketCap());
        existingStock.setPriceChange(newStock.getPriceChange());
        existingStock.setPercentageChange(newStock.getPercentageChange());
        existingStock.setLastUpdated(LocalDateTime.now());
        return stockRepository.save(existingStock);
    }
}
