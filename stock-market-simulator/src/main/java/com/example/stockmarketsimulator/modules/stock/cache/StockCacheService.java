package com.example.stockmarketsimulator.cache;

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
}
