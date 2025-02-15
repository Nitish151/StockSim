package com.example.stockmarketsimulator.modules.stock.controller;

import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.stock.service.StockDataService;
import com.example.stockmarketsimulator.modules.stock.service.StockTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Slf4j
public class StockDataController {

    private final StockTrackingService stockTrackingService;
    private final StockDataService stockDataService;

    /**
     * Endpoint to fetch the latest stock data for the stocks tracked by the user.
     * URL: GET /api/stocks
     * Expects a request header "User-ID" containing the user's ID.
     */
    @GetMapping
    public ResponseEntity<List<Stock>> getUserTrackedStockData(@RequestHeader("User-ID") String userId) {
        log.info("📈 Fetching tracked stock data for user: {}", userId);
        Set<String> trackedSymbols = stockTrackingService.getUserTrackedStocks(userId);
        List<Stock> stocks = trackedSymbols.stream()
                .map(stockDataService::fetchStockData)
                .collect(Collectors.toList());
        return ResponseEntity.ok(stocks);
    }

    /**
     * Endpoint to fetch all globally tracked stock symbols.
     * URL: GET /api/stocks/tracked
     */
    @GetMapping("/tracked")
    public ResponseEntity<Set<String>> getAllTrackedStocks() {
        log.info("📊 Fetching all globally tracked stocks...");
        Set<String> globalTracked = stockTrackingService.getAllTrackedStocks();
        return ResponseEntity.ok(globalTracked);
    }

    /**
     * Endpoint to fetch stock from stock symbol.
     * URL: GET /api/stocks/{symbol}
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<Stock> getStock(@PathVariable String symbol) {
        log.info("📊 Fetching stock %s", symbol);
        Stock stock = stockDataService.fetchStockData(symbol);
        return ResponseEntity.ok(stock);
    }
}
