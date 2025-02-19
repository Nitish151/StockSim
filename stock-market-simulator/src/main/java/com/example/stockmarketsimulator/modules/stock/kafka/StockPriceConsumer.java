package com.example.stockmarketsimulator.modules.stock.kafka;

import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class StockPriceConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String STOCK_CACHE_PREFIX = "stock:";
    private static final Duration STOCK_TTL = Duration.ofMinutes(100); // Expiry time

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
            StockDto stockDto = StockDto.builder()
                    .symbol(data.get("symbol"))
                    .shortName(data.getOrDefault("shortName", "N/A"))
                    .longName(data.getOrDefault("longName", "N/A"))
                    .exchange(data.getOrDefault("exchange", "Unknown"))
                    .marketState(data.getOrDefault("marketState", "Unknown"))
                    .regularMarketPrice(parseBigDecimal(data.get("regularMarketPrice")))
                    .regularMarketChange(parseBigDecimal(data.get("regularMarketChange")))
                    .regularMarketChangePercent(parseBigDecimal(data.get("regularMarketChangePercent")))
                    .regularMarketPreviousClose(parseBigDecimal(data.get("regularMarketPreviousClose")))
                    .regularMarketOpen(parseBigDecimal(data.get("regularMarketOpen")))
                    .regularMarketDayHigh(parseBigDecimal(data.get("regularMarketDayHigh")))
                    .regularMarketDayLow(parseBigDecimal(data.get("regularMarketDayLow")))
                    .regularMarketVolume(parseLong(data.get("regularMarketVolume")))
                    .marketCap(parseBigDecimal(data.get("marketCap")))
                    .fiftyTwoWeekRange(data.getOrDefault("fiftyTwoWeekRange", "N/A"))
                    .fiftyTwoWeekHigh(parseBigDecimal(data.get("fiftyTwoWeekHigh")))
                    .fiftyTwoWeekLow(parseBigDecimal(data.get("fiftyTwoWeekLow")))
                    .fiftyTwoWeekHighChangePercent(parseBigDecimal(data.get("fiftyTwoWeekHighChangePercent")))
                    .fiftyDayAverage(parseBigDecimal(data.get("fiftyDayAverage")))
                    .twoHundredDayAverage(parseBigDecimal(data.get("twoHundredDayAverage")))
                    .trailingPE(parseBigDecimal(data.get("trailingPE")))
                    .forwardPE(parseBigDecimal(data.get("forwardPE")))
                    .epsTrailingTwelveMonths(parseBigDecimal(data.get("epsTrailingTwelveMonths")))
                    .dividendYield(parseBigDecimal(data.get("dividendYield")))
                    .priceToBook(parseBigDecimal(data.get("priceToBook")))
                    .lastUpdated(LocalDateTime.now())
                    .build();

            redisTemplate.opsForValue().set(STOCK_CACHE_PREFIX + symbol, stockDto, STOCK_TTL);
            log.info("✅ Updated stock price in Redis for 100 minutes for: {}", symbol);
        } catch (Exception e) {
            log.error("❌ Error processing stock data for {}: {}", symbol, e.getMessage(), e);
        }
    }

    private Map<String, String> parseStockData(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> rawData = objectMapper.readValue(message, Map.class);
            Map<String, String> data = new HashMap<>();
            for (Map.Entry<String, Object> entry : rawData.entrySet()) {
                if (entry.getValue() != null) {
                    data.put(entry.getKey(), entry.getValue().toString());
                }
            }

            log.info("Parsed data: {}", data);
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
