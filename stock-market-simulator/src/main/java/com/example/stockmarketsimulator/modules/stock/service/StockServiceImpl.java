package com.example.stockmarketsimulator.modules.stock.service;

import com.example.stockmarketsimulator.modules.stock.cache.StockCacheService;
import com.example.stockmarketsimulator.modules.stock.client.YahooFinanceClient;
import com.example.stockmarketsimulator.modules.stock.dto.NewsResponseDto;
import com.example.stockmarketsimulator.modules.stock.dto.SearchResponseDto;
import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.example.stockmarketsimulator.modules.stock.mapper.StockMapper;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.stock.repository.StockRepository;
import com.example.stockmarketsimulator.modules.transaction.model.LimitOrder;
import com.example.stockmarketsimulator.modules.transaction.repository.LimitOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockService {

    private final YahooFinanceClient yahooFinanceClient;
    private final StockCacheService stockCacheService;
    private final StockRepository stockRepository;
    private final StockMapper stockMapper;
    private final LimitOrderRepository limitOrderRepository;


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

    @Override
    public NewsResponseDto getNews(String tickers, String type) {
        // First check the cache
        NewsResponseDto cachedNews = stockCacheService.getCachedNews(tickers, type);
        if (cachedNews != null) {
            log.info("Returning cached news for tickers: {} and type: {}", tickers, type);
            return cachedNews;
        }

        // If not cached, fetch from external API
        log.info("Cache miss for news with tickers: {} and type: {}. Fetching from API.", tickers, type);
        NewsResponseDto newsResponse = yahooFinanceClient.fetchNews(tickers, type);

        if (newsResponse == null) {
            log.warn("No news data received for tickers: {} and type: {}", tickers, type);
            return null;
        }

        // Cache the news response
        stockCacheService.cacheNews(newsResponse, tickers, type);
        return newsResponse;
    }

    @Override
    public Optional<Stock> findById(Long id){
        return stockRepository.findById(id);

    }

    @Override
    public List<Stock> getStocksWithPendingLimitOrders() {
        // Find all stocks that have pending limit orders
        List<Long> stockIdsWithPendingOrders = limitOrderRepository.findByStatus(LimitOrder.LimitOrderStatus.PENDING)
                .stream()
                .map(order -> order.getStock().getId())
                .distinct()
                .collect(Collectors.toList());

        return stockRepository.findAllById(stockIdsWithPendingOrders);
    }

    @Override
    public Stock refreshStockPrice(String symbol) {
        log.info("Refreshing stock price for {}", symbol);

        // Fetch the latest data from the external API
        StockDto stockDto = yahooFinanceClient.fetchStockData(symbol);

        // Update cache with new data
        stockCacheService.cacheStock(symbol, stockDto);

        // Update database and return the updated stock
        Stock updatedStock = saveOrUpdateStock(stockDto);

        log.info("Refreshed price for {}: {}", symbol, updatedStock.getCurrentPrice());
        return updatedStock;
    }

}