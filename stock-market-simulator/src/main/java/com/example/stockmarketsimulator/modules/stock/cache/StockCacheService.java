package com.example.stockmarketsimulator.modules.stock.cache;

import com.example.stockmarketsimulator.modules.stock.dto.NewsDto;
import com.example.stockmarketsimulator.modules.stock.dto.NewsResponseDto;
import com.example.stockmarketsimulator.modules.stock.dto.SearchResponseDto;
import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockCacheService {

    private static final String STOCK_CACHE_PREFIX = "stock:";
    private static final String SEARCH_CACHE_PREFIX = "search";
    private static final String NEWS_CACHE_PREFIX = "news";
    private static final long CACHE_TTL_MINUTES = 100; // Cache expiry time in minutes

    private final RedisTemplate<String, Object> redisTemplate;

    public StockDto getCachedStock(String symbol) {
        String key = STOCK_CACHE_PREFIX + symbol;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            log.info("Returning cached data for symbol: {}", symbol);
            return (StockDto) cached;
        }
        return null;
    }

    public void cacheStock(String symbol, StockDto stockDto) {
        String key = STOCK_CACHE_PREFIX + symbol;
        redisTemplate.opsForValue().set(key, stockDto, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("Cached stock {} for {} minutes", symbol, CACHE_TTL_MINUTES);
    }

    public void cacheSearchStock(String stockName, SearchResponseDto response){
        String key = SEARCH_CACHE_PREFIX + ":" + stockName;
        redisTemplate.opsForValue().set(key, response, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("Cached search results {} for {} minutes", stockName, CACHE_TTL_MINUTES);
    }

    public SearchResponseDto getCachedSearchStock(String stockName){
        String key = SEARCH_CACHE_PREFIX + ":" + stockName;
        Object cached = redisTemplate.opsForValue().get(key);

        if(cached != null) {
            log.info("Returning cached data for stockSymbol: {}", stockName);
            return (SearchResponseDto) cached;
        }
        return null;
    }

    public void cacheNews(NewsResponseDto news, String tickers, String type) {
        StringBuilder keyBuilder = new StringBuilder(NEWS_CACHE_PREFIX);

        if (tickers != null && !tickers.isBlank()) {
            keyBuilder.append(":").append(tickers);
        }

        if (type != null && !type.isBlank()) {
            keyBuilder.append(":").append(type);
        }

        String key = keyBuilder.toString();

        redisTemplate.opsForValue().set(key, news, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        log.info("Cached news for key '{}' for {} minutes", key, CACHE_TTL_MINUTES);
    }

    public NewsResponseDto getCachedNews(String tickers, String type) {
        StringBuilder keyBuilder = new StringBuilder(NEWS_CACHE_PREFIX);

        if (tickers != null && !tickers.isBlank()) {
            keyBuilder.append(":").append(tickers);
        }

        if (type != null && !type.isBlank()) {
            keyBuilder.append(":").append(type);
        }

        String key = keyBuilder.toString();

        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            log.info("Returning cached news for key '{}'", key);
            return (NewsResponseDto) cached;
        }
        return null;
    }

}