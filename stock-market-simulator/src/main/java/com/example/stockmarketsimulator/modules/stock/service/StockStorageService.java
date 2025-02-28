package com.example.stockmarketsimulator.modules.stock.service;

import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class StockStorageService {
    private final StockRepository stockRepository;
    private final StockDataService stockDataService;

    public Stock findStockBySymbol(String symbol) {
        // Fetch stock data from the external service
        StockDto stockDto = stockDataService.fetchStockData(symbol);

        // Map StockDto to Stock entity
        Stock stock = Stock.builder()
                .symbol(stockDto.getSymbol())
                .companyName(stockDto.getLongName()) // Use longName as companyName
                .industry("Unknown") // Set a default or fetch from another source
                .currentPrice(stockDto.getRegularMarketPrice())
                .openingPrice(stockDto.getRegularMarketOpen())
                .previousClose(stockDto.getRegularMarketPreviousClose())
                .volume(stockDto.getRegularMarketVolume())
                .marketCap(stockDto.getMarketCap())
                .priceChange(stockDto.getRegularMarketChange())
                .percentageChange(stockDto.getRegularMarketChangePercent())
                .lastUpdated(stockDto.getLastUpdated())
                .build();

        saveOrUpdateStock(stock);

        return stock;
    }

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
