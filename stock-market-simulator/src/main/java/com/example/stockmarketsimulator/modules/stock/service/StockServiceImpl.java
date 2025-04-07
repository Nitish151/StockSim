package com.example.stockmarketsimulator.modules.stock.service;

import com.example.stockmarketsimulator.modules.stock.cache.StockCacheService;
import com.example.stockmarketsimulator.modules.stock.client.YahooFinanceClient;
import com.example.stockmarketsimulator.modules.stock.dto.SearchResponseDto;
import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.example.stockmarketsimulator.modules.stock.mapper.StockMapper;
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
public class StockServiceImpl implements StockService {

    private final YahooFinanceClient yahooFinanceClient;
    private final StockCacheService stockCacheService;
    private final StockRepository stockRepository;
    private final StockMapper stockMapper;

    @Override
    public StockDto getStockData(String symbol) {
        // First check cache
        StockDto cachedStock = stockCacheService.getCachedStock(symbol);
        if (cachedStock != null) {
            log.info("Returning cached data for symbol: {}", symbol);
            return cachedStock;
        }

        // If not cached, fetch from API
        log.info("Cache miss for symbol: {}. Fetching from API.", symbol);
        StockDto stockDto = yahooFinanceClient.fetchStockData(symbol);

        // Cache the fetched data
        stockCacheService.cacheStock(symbol, stockDto);

        return stockDto;
    }

    @Override
    public Stock getAndPersistStock(String symbol) {
        StockDto stockDto = getStockData(symbol);
        return saveOrUpdateStock(stockDto);
    }

    private Stock saveOrUpdateStock(StockDto stockDto) {
        Optional<Stock> existingStockOpt = stockRepository.findBySymbol(stockDto.getSymbol());

        if (existingStockOpt.isPresent()) {
            Stock existingStock = existingStockOpt.get();
            // Update fields
            updateStockFields(existingStock, stockDto);
            stockRepository.save(existingStock);
            log.info("Updated stock in DB: {}", stockDto.getSymbol());
            return existingStock;
        } else {
            // Convert DTO to entity and save
            Stock newStock = stockMapper.toStockEntity(stockDto);
            stockRepository.save(newStock);
            log.info("Saved new stock in DB: {}", stockDto.getSymbol());
            return newStock;
        }
    }

    private void updateStockFields(Stock stock, StockDto dto) {
        stock.setCompanyName(dto.getLongName());
        stock.setCurrentPrice(dto.getRegularMarketPrice());
        stock.setOpeningPrice(dto.getRegularMarketOpen());
        stock.setPreviousClose(dto.getRegularMarketPreviousClose());
        stock.setVolume(dto.getRegularMarketVolume());
        stock.setMarketCap(dto.getMarketCap());
        stock.setPriceChange(dto.getRegularMarketChange());
        stock.setPercentageChange(dto.getRegularMarketChangePercent());
        stock.setLastUpdated(LocalDateTime.now());
    }

    @Override
    public SearchResponseDto searchStocksByName(String stockName){
        SearchResponseDto cachedSearchResponse = stockCacheService.getCachedSearchStock(stockName);

        if (cachedSearchResponse != null) {
            log.info("Returning cached data for symbol: {}", stockName);
            return cachedSearchResponse;
        }

        // If not cached, fetch from API
        log.info("Cache miss for stcokName: {}. Fetching from API.", stockName);
        SearchResponseDto searchResponseDto = yahooFinanceClient.searchStocks(stockName);

        if(searchResponseDto == null){
            log.info("No stocks for search string {}", stockName);
        }
        // Cache the fetched data
        stockCacheService.cacheSearchStock(stockName, searchResponseDto);

        return searchResponseDto;
    }
}