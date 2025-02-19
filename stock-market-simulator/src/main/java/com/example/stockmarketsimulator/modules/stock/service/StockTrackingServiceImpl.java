package com.example.stockmarketsimulator.modules.stock.service;

import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockTrackingServiceImpl implements StockTrackingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final StockStorageService stockStorageService;
    private final StockDataService stockDataService;

    // Key format for per-user tracked stocks, e.g., user:stocks:A123
    private static final String USER_TRACK_KEY = "user:stocks:%s";
    // Global set for all unique tracked stocks (used by the producer)
    private static final String GLOBAL_TRACK_KEY = "tracked_stocks";

    /**
     * Adds the given stock symbol to the user's tracked set and to the global set.
     * Sets an expiry of 10 minutes on the per-user set.
     */
    @Override
    public void trackStock(String username, String symbol) {
        String userKey = String.format(USER_TRACK_KEY, username);
        redisTemplate.opsForSet().add(userKey, symbol);
        redisTemplate.expire(userKey, Duration.ofMinutes(100));
        redisTemplate.opsForSet().add(GLOBAL_TRACK_KEY, symbol);
        redisTemplate.expire(GLOBAL_TRACK_KEY, Duration.ofMinutes(100));
        log.info("📌 User {} is now tracking stock: {}", username, symbol);
    }

    /**
     * Removes the given stock symbol from the user's tracked set.
     * If no user is tracking the symbol, it also removes it from the global set.
     */
    @Override
    public void untrackStock(String username, String symbol) {
        String userKey = String.format(USER_TRACK_KEY, username);
        redisTemplate.opsForSet().remove(userKey, symbol);
        if (isStockUntracked(symbol)) {
            redisTemplate.opsForSet().remove(GLOBAL_TRACK_KEY, symbol);
            log.info("🗑️ {} removed from global tracking (no user is tracking it)", symbol);
        }
        log.info("❌ User {} stopped tracking stock: {}", username, symbol);
    }

    /**
     * Returns the set of stock symbols tracked by the specified user.
     */
    @Override
    public Set<String> getUserTrackedStocks(String username) {
        String userKey = String.format(USER_TRACK_KEY, username);
        Set<Object> stocks = redisTemplate.opsForSet().members(userKey);
        return stocks != null ? stocks.stream().map(Object::toString).collect(Collectors.toSet()) : new HashSet<>();
    }

    /**
     * Returns the global set of unique tracked stock symbols.
     */
    @Override
    public Set<String> getAllTrackedStocks() {
        Set<Object> stocks = redisTemplate.opsForSet().members(GLOBAL_TRACK_KEY);
        return stocks != null ? stocks.stream().map(Object::toString).collect(Collectors.toSet()) : new HashSet<>();
    }

    /**
     * Checks if no user is currently tracking the given symbol.
     */
    @Override
    public boolean isStockUntracked(String symbol) {
        Set<String> keys = redisTemplate.keys(String.format(USER_TRACK_KEY, "*"));
        if (keys == null || keys.isEmpty()) {
            return true;
        }
        return keys.stream().noneMatch(key -> Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, symbol)));
    }

    @Override
    public Stock fetchAndStoreStock(String symbol) {
        // 1️⃣ Fetch stock data from API
        StockDto stockDto = stockDataService.fetchStockData(symbol);

        // 2️⃣ Convert DTO to Stock entity
        Stock stock = Stock.builder()
                .symbol(stockDto.getSymbol())
                .companyName(stockDto.getLongName()) // Assuming longName maps to companyName
                .industry("N/A") // Placeholder (not available in API)
                .currentPrice(stockDto.getRegularMarketPrice())
                .openingPrice(stockDto.getRegularMarketOpen())
                .previousClose(stockDto.getRegularMarketPreviousClose())
                .volume(stockDto.getRegularMarketVolume())
                .marketCap(stockDto.getMarketCap())
                .priceChange(stockDto.getRegularMarketChange())
                .percentageChange(stockDto.getRegularMarketChangePercent())
                .lastUpdated(stockDto.getLastUpdated())
                .build();

        // 3️⃣ Save to database
        return stockStorageService.saveOrUpdateStock(stock);
    }

}
