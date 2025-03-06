package com.example.stockmarketsimulator.modules.stock.kafka;

import com.example.stockmarketsimulator.cache.StockCacheService;
import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockPriceConsumer {

    private final ObjectMapper objectMapper;
    private final StockCacheService stockCacheService;

    @KafkaListener(topics = "stock-prices", groupId = "stock-group")
    public void consumeStockPrice(String message) {
        try {
            // Convert JSON message into a StockDto
            StockDto stockDto = objectMapper.readValue(message, StockDto.class);
            stockCacheService.cacheStock(stockDto.getSymbol(), stockDto);
            log.info("Updated cache with stock data for: {}", stockDto.getSymbol());
        } catch (Exception e) {
            log.error("Error processing Kafka message: {}", e.getMessage(), e);
        }
    }
}