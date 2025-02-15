package com.example.stockmarketsimulator.modules.stock.kafka;

import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static yahoofinance.Utils.getBigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockPriceConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String STOCK_CACHE_PREFIX = "stock:";
    private static final Duration STOCK_TTL = Duration.ofMinutes(3); // Expiry time

    @KafkaListener(topics = "stock-prices", groupId = "stock-group")
    public void consumeStockPrice(String message) {
        log.info("📥 Received stock data from Kafka: {}", message);

        Map<String, String> data = parseStockData(message);
        if (data == null) {
            log.warn("⚠️ Invalid stock data received. Skipping...");
            return;
        }

        String symbol = data.get("symbol");

        try {
            Stock stock = Stock.builder()
                    .symbol(data.get("symbol"))
                    .companyName(data.getOrDefault("longName", "Unknown"))
                    .industry(data.getOrDefault("industry", "Unknown")) // Use getOrDefault for optional fields
                    .currentPrice(new BigDecimal(data.get("regularMarketPrice"))) // Directly create BigDecimal
                    .openingPrice(new BigDecimal(data.get("regularMarketOpen"))) // Directly create BigDecimal
                    .previousClose(new BigDecimal(data.get("regularMarketPreviousClose"))) // Directly create BigDecimal
                    .volume(Long.parseLong(data.getOrDefault("regularMarketVolume", "0"))) // Parse Long
                    .marketCap(new BigDecimal(data.get("marketCap"))) // Directly create BigDecimal
                    .priceChange(new BigDecimal(data.get("regularMarketChange"))) // Directly create BigDecimal
                    .percentageChange(new BigDecimal(data.get("regularMarketChangePercent"))) // Directly create BigDecimal
                    .lastUpdated(LocalDateTime.now())
                    .build();

            redisTemplate.opsForValue().set(STOCK_CACHE_PREFIX + symbol, stock, STOCK_TTL);
            log.info("✅ Updated stock price in Redis for: {}", symbol);
        } catch (Exception e) {
            log.error("❌ Error processing stock data for {}: {}", symbol, e.getMessage(), e);
        }
    }

    private Map<String, String> parseStockData(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> rawData = objectMapper.readValue(message, Map.class);
            Map<String, String> data = new HashMap<>();
            for(Map.Entry<String, Object> entry : rawData.entrySet()) {
                if(entry.getValue() != null) {
                    data.put(entry.getKey(), entry.getValue().toString());
                }
            }

            log.info("parsed data: {}", data);
            return data;
        } catch (Exception e) {
            log.error("🔍 Error parsing stock data: {}", message, e);
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return (value != null && !value.isEmpty()) ? new BigDecimal(value) : BigDecimal.ZERO;
        } catch (NumberFormatException e) {
            log.warn("⚠️ Invalid number format for value: {}", value);
            return BigDecimal.ZERO;
        }
    }

    private Long parseLong(String value) {
        try {
            return (value != null && !value.isEmpty()) ? Long.parseLong(value) : 0L;
        } catch (NumberFormatException e) {
            log.warn("⚠️ Invalid number format for value: {}", value);
            return 0L;
        }
    }
}
