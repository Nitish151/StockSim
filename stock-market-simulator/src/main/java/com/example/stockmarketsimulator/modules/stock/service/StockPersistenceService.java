package com.example.stockmarketsimulator.modules.stock.service;

import com.example.stockmarketsimulator.cache.StockCacheService;
import com.example.stockmarketsimulator.modules.stock.client.YahooFinanceClient;
import com.example.stockmarketsimulator.modules.stock.mapper.StockMapper;
import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockPersistenceService {

    private final YahooFinanceClient yahooFinanceClient;
    private final StockCacheService stockCacheService;
    private final StockRepository stockRepository;
    private final StockMapper stockMapper;

    /**
     * Retrieves stock data for the given symbol, either from cache or via the API,
     * and then persists (or updates) it in the database.
     */
    public Stock findAndPersistStock(String symbol) {
        // Check cache first
        StockDto stockDto = stockCacheService.getCachedStock(symbol);
        if (stockDto == null) {
            log.info("Cache miss for symbol: {}. Fetching from API.", symbol);
            stockDto = yahooFinanceClient.fetchStockData(symbol);
            stockCacheService.cacheStock(symbol, stockDto);
        } else {
            log.info("Using cached stock data for symbol: {}", symbol);
        }
        return saveOrUpdateStock(stockDto);
    }

    private Stock saveOrUpdateStock(StockDto stockDto) {
        Optional<Stock> existingStockOpt = stockRepository.findBySymbol(stockDto.getSymbol());
        if (existingStockOpt.isPresent()) {
            Stock existingStock = existingStockOpt.get();
            // Update fields
            existingStock.setCompanyName(stockDto.getLongName());
            existingStock.setCurrentPrice(stockDto.getRegularMarketPrice());
            existingStock.setOpeningPrice(stockDto.getRegularMarketOpen());
            existingStock.setPreviousClose(stockDto.getRegularMarketPreviousClose());
            existingStock.setVolume(stockDto.getRegularMarketVolume());
            existingStock.setMarketCap(stockDto.getMarketCap());
            existingStock.setPriceChange(stockDto.getRegularMarketChange());
            existingStock.setPercentageChange(stockDto.getRegularMarketChangePercent());
            existingStock.setLastUpdated(LocalDateTime.now());
            stockRepository.save(existingStock);
            log.info("Updated stock in DB: {}", stockDto.getSymbol());
            return existingStock;
        } else {
            // Convert DTO to entity
            Stock newStock = stockMapper.toStockEntity(stockDto);
            stockRepository.save(newStock);
            log.info("Saved new stock in DB: {}", stockDto.getSymbol());
            return newStock;
        }
    }
}
