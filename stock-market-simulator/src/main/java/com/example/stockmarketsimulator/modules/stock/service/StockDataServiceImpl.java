package com.example.stockmarketsimulator.modules.stock.service;

import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.stock.repository.StockRepository;
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockDataServiceImpl implements StockDataService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final StockRepository stockRepository; // Inject StockRepository

    private static final String STOCK_CACHE_PREFIX = "stock:";
    private static final String API_URL = "https://yahoo-finance15.p.rapidapi.com/api/v1/markets/stock/quotes?ticker=";
    private static final String API_HOST = "yahoo-finance15.p.rapidapi.com";
    private static final String API_KEY = "0086b8c5f0msh01319a52ee632cfp159702jsn7718a5462c76"; // Replace with actual API key

    @Override
    public StockDto fetchStockData(String symbol) {
        String cacheKey = STOCK_CACHE_PREFIX + symbol;

        // 1️⃣ Check if stock data is cached
        StockDto cachedStock = (StockDto) redisTemplate.opsForValue().get(cacheKey);
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

            // 5️⃣ Map to StockDto
            StockDto stockDto = StockDto.builder()
                    .symbol(stockNode.path("symbol").asText())
                    .shortName(stockNode.path("shortName").asText("N/A"))
                    .longName(stockNode.path("longName").asText("N/A"))
                    .exchange(stockNode.path("fullExchangeName").asText("Unknown"))
                    .marketState(stockNode.path("marketState").asText("Unknown"))
                    .regularMarketPrice(getBigDecimal(stockNode, "regularMarketPrice"))
                    .regularMarketChange(getBigDecimal(stockNode, "regularMarketChange"))
                    .regularMarketChangePercent(getBigDecimal(stockNode, "regularMarketChangePercent"))
                    .regularMarketPreviousClose(getBigDecimal(stockNode, "regularMarketPreviousClose"))
                    .regularMarketOpen(getBigDecimal(stockNode, "regularMarketOpen"))
                    .regularMarketDayHigh(getBigDecimal(stockNode, "regularMarketDayHigh"))
                    .regularMarketDayLow(getBigDecimal(stockNode, "regularMarketDayLow"))
                    .regularMarketVolume(stockNode.path("regularMarketVolume").asLong(0L))
                    .marketCap(getBigDecimal(stockNode, "marketCap"))
                    .fiftyTwoWeekRange(stockNode.path("fiftyTwoWeekRange").asText())
                    .fiftyTwoWeekHigh(getBigDecimal(stockNode, "fiftyTwoWeekHigh"))
                    .fiftyTwoWeekLow(getBigDecimal(stockNode, "fiftyTwoWeekLow"))
                    .fiftyTwoWeekHighChangePercent(getBigDecimal(stockNode, "fiftyTwoWeekHighChangePercent"))
                    .fiftyDayAverage(getBigDecimal(stockNode, "fiftyDayAverage"))
                    .twoHundredDayAverage(getBigDecimal(stockNode, "twoHundredDayAverage"))
                    .trailingPE(getBigDecimal(stockNode, "trailingPE"))
                    .forwardPE(getBigDecimal(stockNode, "forwardPE"))
                    .epsTrailingTwelveMonths(getBigDecimal(stockNode, "epsTrailingTwelveMonths"))
                    .dividendYield(getBigDecimal(stockNode, "dividendYield"))
                    .priceToBook(getBigDecimal(stockNode, "priceToBook"))
                    .lastUpdated(LocalDateTime.now())
                    .build();

            saveOrUpdateStock(stockDto);

            // 8️⃣ Cache data
            redisTemplate.opsForValue().set(cacheKey, stockDto, 100, TimeUnit.MINUTES);
            log.info("📥 Cached stock data for {} with 100-minute expiry", symbol);

            return stockDto;
        } catch (Exception e) {
            log.error("🚨 Error parsing stock data for {}: {}", symbol, e.getMessage(), e);
            throw new RuntimeException("Failed to parse stock data", e);
        }
    }

    private void saveOrUpdateStock(StockDto stockDto) {
        // Check if the stock already exists in the database

        Optional<Stock> existingStock = stockRepository.findBySymbol(stockDto.getSymbol());

        if (existingStock.isPresent()) {
            // Update the existing stock

            Stock updatedStock = existingStock.get();
            updatedStock.setCompanyName(stockDto.getLongName());
            updatedStock.setIndustry("N/A"); // Not provided in API, placeholder
            updatedStock.setCurrentPrice(stockDto.getRegularMarketPrice());
            updatedStock.setOpeningPrice(stockDto.getRegularMarketOpen());
            updatedStock.setPreviousClose(stockDto.getRegularMarketPreviousClose());
            updatedStock.setVolume(stockDto.getRegularMarketVolume());
            updatedStock.setMarketCap(stockDto.getMarketCap());
            updatedStock.setPriceChange(stockDto.getRegularMarketChange());
            updatedStock.setPercentageChange(stockDto.getRegularMarketChangePercent());
            updatedStock.setLastUpdated(LocalDateTime.now());

            stockRepository.save(updatedStock);
            log.info("🔄 Updated stock in database: {}", updatedStock.getSymbol());
        } else {
            // Save the new stock
            Stock stock = Stock.builder()
                    .symbol(stockDto.getSymbol())
                    .companyName(stockDto.getLongName()) // Mapping long name to companyName
                    .industry("N/A") // Not provided in API, placeholder
                    .currentPrice(stockDto.getRegularMarketPrice())
                    .openingPrice(stockDto.getRegularMarketOpen())
                    .previousClose(stockDto.getRegularMarketPreviousClose())
                    .volume(stockDto.getRegularMarketVolume())
                    .marketCap(stockDto.getMarketCap())
                    .priceChange(stockDto.getRegularMarketChange())
                    .percentageChange(stockDto.getRegularMarketChangePercent())
                    .lastUpdated(LocalDateTime.now())
                    .build();
            stockRepository.save(stock);
            log.info("✅ Saved new stock to database: {}", stock.getSymbol());
        }
    }

    private BigDecimal getBigDecimal(JsonNode node, String field) {
        return node.has(field) && node.get(field).isNumber() ? node.get(field).decimalValue() : BigDecimal.ZERO;
    }
}