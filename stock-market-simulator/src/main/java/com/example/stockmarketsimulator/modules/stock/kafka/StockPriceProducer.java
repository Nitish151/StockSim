package com.example.stockmarketsimulator.modules.stock.kafka;

import com.example.stockmarketsimulator.modules.stock.client.YahooFinanceClient;
import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockPriceProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final YahooFinanceClient yahooFinanceClient;
    private final ObjectMapper objectMapper;

    private static final String TRACKED_STOCKS_KEY = "tracked_stocks";
    private static final String TOPIC = "stock-prices";

    @Scheduled(fixedRate = 1000000) // Adjust the rate as needed
    public void fetchAndSendStockPrice() {
        log.info("🚀 Scheduled Task: Fetching stock prices...");

        Set<Object> symbolsSet = redisTemplate.opsForSet().members(TRACKED_STOCKS_KEY);
        if (symbolsSet == null || symbolsSet.isEmpty()) {
            log.warn("⚠️ No stocks being tracked.");
            return;
        }

        // Convert raw symbols to a Set of Strings
        Set<String> symbols = symbolsSet.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());

        // For each tracked symbol, fetch stock data and send to Kafka
        symbols.forEach(symbol -> {
            try {
                StockDto stockDto = yahooFinanceClient.fetchStockData(symbol);
                // Convert StockDto to JSON
                String message = objectMapper.writeValueAsString(stockDto);
                kafkaTemplate.send(TOPIC, symbol, message);
                log.info("📤 Sent stock data for symbol: {}", symbol);
            } catch (Exception e) {
                log.error("❌ Error fetching/sending data for {}: {}", symbol, e.getMessage(), e);
            }
        });
    }
}
