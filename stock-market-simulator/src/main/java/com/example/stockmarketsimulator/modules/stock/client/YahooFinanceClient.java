package com.example.stockmarketsimulator.modules.stock.client;

import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.example.stockmarketsimulator.modules.stock.mapper.StockMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class YahooFinanceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final StockMapper stockMapper;

    @Value("${yahoo.api.url}")
    private String apiUrl;

    @Value("${yahoo.api.host}")
    private String apiHost;

    @Value("${yahoo.api.key}")
    private String apiKey;

    public StockDto fetchStockData(String symbol) {
        String url = apiUrl + symbol;
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-rapidapi-host", apiHost);
        headers.set("x-rapidapi-key", apiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("API returned invalid response for symbol: {}", symbol);
            throw new RuntimeException("API returned invalid response");
        }

        try {
            JsonNode rootNode = objectMapper.readTree(response.getBody());
            JsonNode bodyNode = rootNode.path("body");
            if (!bodyNode.isArray() || bodyNode.size() == 0) {
                log.error("No data in API response for symbol: {}", symbol);
                throw new RuntimeException("No data in API response");
            }
            JsonNode stockNode = bodyNode.get(0);
            return stockMapper.toStockDto(stockNode);
        } catch (Exception e) {
            log.error("Error parsing API response for symbol: {}", symbol, e);
            throw new RuntimeException("Error parsing API response", e);
        }
    }
}