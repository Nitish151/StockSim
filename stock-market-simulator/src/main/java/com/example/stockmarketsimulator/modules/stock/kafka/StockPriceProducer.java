package com.example.stockmarketsimulator.modules.stock.kafka;

import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockPriceProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "stock-prices";
    private static final String TRACKED_STOCKS_KEY = "tracked_stocks";
    private static final String API_URL = "https://yahoo-finance15.p.rapidapi.com/api/v1/markets/stock/quotes?ticker=";
    private static final String API_HOST = "yahoo-finance15.p.rapidapi.com";
    private static final String API_KEY = "0086b8c5f0msh01319a52ee632cfp159702jsn7718a5462c76"; // Replace with actual API key

    @Scheduled(fixedRate = 1000000) // Runs every 5 minutes
    public void fetchAndSendStockPrice() {
        log.info("🚀 Scheduled Task: Fetching stock prices...");

        Set<Object> symbols = redisTemplate.opsForSet().members(TRACKED_STOCKS_KEY);
        if (symbols == null || symbols.isEmpty()) {
            log.warn("⚠️ No stocks being tracked.");
            return;
        }

        String tickerSymbols = symbols.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));

        try {
            log.info("🌍 Fetching stock data for: {}", tickerSymbols);

            HttpHeaders headers = new HttpHeaders();
            headers.set("x-rapidapi-host", API_HOST);
            headers.set("x-rapidapi-key", API_KEY);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(API_URL + tickerSymbols, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String jsonData = response.getBody();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(jsonData);
                JsonNode bodyNode = rootNode.path("body");
                if(bodyNode.isArray()) {
                    for(JsonNode jsonNode : bodyNode){
                        String data = String.valueOf(jsonNode);
                        String symbol = jsonNode.path("symbol").asText();
                        kafkaTemplate.send(TOPIC, symbol, data);
                    }
                }


            } else {
                log.error("❌ Failed to fetch stock data. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("🚨 Error fetching stock data: {}", e.getMessage(), e);
        }
    }


    private BigDecimal getBigDecimal(JsonNode node, String field) {
        return node.has(field) && node.get(field).isNumber() ? node.get(field).decimalValue() : BigDecimal.ZERO;
    }
}
