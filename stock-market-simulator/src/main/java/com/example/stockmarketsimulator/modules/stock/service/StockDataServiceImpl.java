package com.example.stockmarketsimulator.modules.stock.service;

import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockDataServiceImpl implements StockDataService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String STOCK_CACHE_PREFIX = "stock:";
    private static final String API_URL = "https://yahoo-finance15.p.rapidapi.com/api/v1/markets/stock/quotes?ticker=";
    private static final String API_HOST = "yahoo-finance15.p.rapidapi.com";
    private static final String API_KEY = "0086b8c5f0msh01319a52ee632cfp159702jsn7718a5462c76"; // Replace with actual API key

    @Override
    public Stock fetchStockData(String symbol) {
        String cacheKey = STOCK_CACHE_PREFIX + symbol;

        // 1️⃣ Check if stock data is cached
        Stock cachedStock = (Stock) redisTemplate.opsForValue().get(cacheKey);
        if (cachedStock != null) {
            log.info("✅ Returning cached data for {}", symbol);
            return cachedStock;
        }

        log.info("🌍 Fetching stock data from RapidAPI for {}", symbol);

        // 2️⃣ Prepare headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", API_HOST);
        headers.set("x-rapidapi-key", API_KEY);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 3️⃣ Fetch stock data
        ResponseEntity<String> response = restTemplate.exchange(API_URL + symbol, HttpMethod.GET, entity, String.class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("❌ Empty or invalid response for {}. Status: {}", symbol, response.getStatusCode());
            throw new RuntimeException("API returned empty response");
        }

        try {
            // 4️⃣ Parse response
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode bodyNode = rootNode.path("body");

            // After getting bodyNode:
            if (!bodyNode.isArray() || bodyNode.size() == 0) {
                log.error("❌ Empty body array for symbol {}", symbol);
                throw new RuntimeException("No stock data available");
            }

            JsonNode stockNode = bodyNode.get(0);

            // 5️⃣ Map to Stock model
            Stock stock = Stock.builder()
                    .symbol(stockNode.path("symbol").asText())
                    .companyName(stockNode.path("shortName").asText("N/A"))
                    .industry(stockNode.path("industry").asText("Unknown"))
                    .currentPrice(getBigDecimal(stockNode, "regularMarketPrice"))
                    .openingPrice(getBigDecimal(stockNode, "regularMarketOpen"))
                    .previousClose(getBigDecimal(stockNode, "regularMarketPreviousClose"))
                    .volume(stockNode.path("regularMarketVolume").asLong(0L))
                    .marketCap(getBigDecimal(stockNode, "marketCap"))
                    .priceChange(getBigDecimal(stockNode, "regularMarketChange"))
                    .percentageChange(getBigDecimal(stockNode, "regularMarketChangePercent"))
                    .lastUpdated(LocalDateTime.now())
                    .build();

            // 6️⃣ Cache data
            redisTemplate.opsForValue().set(cacheKey, stock, 1, TimeUnit.MINUTES);
            log.info("📥 Cached stock data for {} with 1-minute expiry", symbol);

            return stock;
        } catch (Exception e) {
            log.error("🚨 Error parsing stock data for {}: {}", symbol, e.getMessage(), e);
            throw new RuntimeException("Failed to parse stock data", e);
        }
    }

    private BigDecimal getBigDecimal(JsonNode node, String field) {
        return node.has(field) && node.get(field).isNumber() ? node.get(field).decimalValue() : BigDecimal.ZERO;
    }
}
