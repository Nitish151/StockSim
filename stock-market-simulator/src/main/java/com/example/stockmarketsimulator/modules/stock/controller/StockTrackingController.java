package com.example.stockmarketsimulator.modules.stock.controller;

import com.example.stockmarketsimulator.modules.stock.service.StockTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
public class StockTrackingController {

    private final StockTrackingService stockTrackingService;

    /**
     * Endpoint to track a stock for a specific user.
     * URL: POST /api/tracking/{userId}/track/{symbol}
     */
    @PostMapping("/{userId}/track/{symbol}")
    public String trackStock(@PathVariable String userId, @PathVariable String symbol) {
        stockTrackingService.trackStock(userId, symbol);
        log.info("📌 User {} is now tracking {}", userId, symbol);
        return "Tracking " + symbol;
    }

    /**
     * Endpoint to untrack a stock for a specific user.
     * URL: DELETE /api/tracking/{userId}/untrack/{symbol}
     */
    @DeleteMapping("/{userId}/untrack/{symbol}")
    public String untrackStock(@PathVariable String userId, @PathVariable String symbol) {
        stockTrackingService.untrackStock(userId, symbol);
        log.info("❌ User {} stopped tracking {}", userId, symbol);
        return "Stopped tracking " + symbol;
    }

    /**
     * Endpoint to get all stocks tracked by a specific user.
     * URL: GET /api/tracking/{userId}/tracked
     */
    @GetMapping("/{userId}/tracked")
    public Set<String> getUserTrackedStocks(@PathVariable String userId) {
        log.info("👤 Fetching tracked stocks for user {}", userId);
        return stockTrackingService.getUserTrackedStocks(userId);
    }
}
