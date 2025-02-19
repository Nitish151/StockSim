package com.example.stockmarketsimulator.modules.stock.controller;

import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.stock.service.StockDataService;
import com.example.stockmarketsimulator.modules.stock.service.StockTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
@Slf4j
public class StockTrackingController {

    private final StockTrackingService stockTrackingService;
    private final StockDataService stockDataService;

    /**
     * Endpoint to fetch the latest stock data for the stocks tracked by the user.
     * URL: GET /api/stocks
     * Expects a request header "User-ID" containing the user's ID.
     */
    @GetMapping
    public ResponseEntity<?> getUserTrackedStockData(@AuthenticationPrincipal UserDetails user) {
        log.info("Inside the getUserTrackedStrockData");
        if (user == null) {
            log.error("❌ AuthenticationPrincipal is NULL!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        String username = user.getUsername();
        log.info("📈 Fetching tracked stock data for user: {}", username);
        Set<String> trackedSymbols = stockTrackingService.getUserTrackedStocks(username);
        List<StockDto> stocks = trackedSymbols.stream()
                .map(stockDataService::fetchStockData)
                .collect(Collectors.toList());
        return ResponseEntity.ok(stocks);
    }

    /**
     * Endpoint to fetch all globally tracked stock symbols.
     * URL: GET /api/stocks/tracked
     */
//    @GetMapping("/tracked")
//    public ResponseEntity<Set<String>> getAllTrackedStocks() {
//        log.info("📊 Fetching all globally tracked stocks...");
//        Set<String> globalTracked = stockTrackingService.getAllTrackedStocks();
//        return ResponseEntity.ok(globalTracked);
//    }

    /**
     * Endpoint to track a stock for a specific user.
     * URL: POST /api/tracking/{userId}/track/{symbol}
     */
    @PostMapping("/{symbol}")
    public String trackStock(@PathVariable String symbol, @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        stockTrackingService.trackStock(username, symbol);
        log.info("📌 User {} is now tracking {}", username, symbol);
        return "Tracking " + symbol;
    }

    /**
     * Endpoint to untrack a stock for a specific user.
     * URL: DELETE /api/tracking/{userId}/untrack/{symbol}
     */
    @DeleteMapping("/{symbol}")
    public String untrackStock(@PathVariable String symbol, @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        stockTrackingService.untrackStock(username, symbol);
        log.info("❌ User {} stopped tracking {}", username, symbol);
        return "Stopped tracking " + symbol;
    }

    /**
     * Endpoint to get all stocks tracked by a specific user.
     * URL: GET /api/tracking/{userId}/tracked
     */
    @GetMapping("/tracked")
    public Set<String> getUserTrackedStocks(@AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        log.info("👤 Fetching tracked stocks for user {}", username);
        return stockTrackingService.getUserTrackedStocks(username);
    }
}
