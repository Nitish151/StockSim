package com.example.stockmarketsimulator.modules.stock.service;

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

    private static final String USER_TRACK_KEY = "user:stocks:%s";
    private static final String GLOBAL_TRACK_KEY = "tracked_stocks";
    private static final long TRACKING_TTL_MINUTES = 100;

    @Override
    public void trackStock(String username, String symbol) {
        String userKey = String.format(USER_TRACK_KEY, username);
        redisTemplate.opsForSet().add(userKey, symbol);
        redisTemplate.expire(userKey, Duration.ofMinutes(TRACKING_TTL_MINUTES));
        redisTemplate.opsForSet().add(GLOBAL_TRACK_KEY, symbol);
        redisTemplate.expire(GLOBAL_TRACK_KEY, Duration.ofMinutes(TRACKING_TTL_MINUTES));
        log.info("User {} is now tracking stock: {}", username, symbol);
    }

    @Override
    public void untrackStock(String username, String symbol) {
        String userKey = String.format(USER_TRACK_KEY, username);
        redisTemplate.opsForSet().remove(userKey, symbol);
        if (isStockUntracked(symbol)) {
            redisTemplate.opsForSet().remove(GLOBAL_TRACK_KEY, symbol);
            log.info("{} removed from global tracking (no user is tracking it)", symbol);
        }
        log.info("User {} stopped tracking stock: {}", username, symbol);
    }

    @Override
    public Set<String> getUserTrackedStocks(String username) {
        String userKey = String.format(USER_TRACK_KEY, username);
        Set<Object> stocks = redisTemplate.opsForSet().members(userKey);
        return stocks != null ?
                stocks.stream().map(Object::toString).collect(Collectors.toSet()) :
                new HashSet<>();
    }

    @Override
    public Set<String> getAllTrackedStocks() {
        Set<Object> stocks = redisTemplate.opsForSet().members(GLOBAL_TRACK_KEY);
        return stocks != null ?
                stocks.stream().map(Object::toString).collect(Collectors.toSet()) :
                new HashSet<>();
    }

    @Override
    public boolean isStockUntracked(String symbol) {
        Set<String> keys = redisTemplate.keys(String.format(USER_TRACK_KEY, "*"));
        if (keys == null || keys.isEmpty()) {
            return true;
        }
        return keys.stream().noneMatch(key ->
                Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, symbol)));
    }
}