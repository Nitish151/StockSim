package com.example.stockmarketsimulator.modules.stock.controller;

import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.example.stockmarketsimulator.modules.stock.service.StockService;
import com.example.stockmarketsimulator.modules.stock.service.StockTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    private final StockService stockService;

    @GetMapping
    public ResponseEntity<?> getUserTrackedStockData(@AuthenticationPrincipal UserDetails user) {
        log.info("Inside the getUserTrackedStockData");
        if (user == null) {
            log.error("AuthenticationPrincipal is NULL!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        String username = user.getUsername();
        log.info("Fetching tracked stock data for user: {}", username);
        Set<String> trackedSymbols = stockTrackingService.getUserTrackedStocks(username);
        List<StockDto> stocks = trackedSymbols.stream()
                .map(stockService::getStockData)
                .collect(Collectors.toList());
        return ResponseEntity.ok(stocks);
    }

    @PostMapping("/{symbol}")
    public String trackStock(@PathVariable String symbol, @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        stockTrackingService.trackStock(username, symbol);
        log.info("User {} is now tracking {}", username, symbol);
        return "Tracking " + symbol;
    }

    @DeleteMapping("/{symbol}")
    public String untrackStock(@PathVariable String symbol, @AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        stockTrackingService.untrackStock(username, symbol);
        log.info("User {} stopped tracking {}", username, symbol);
        return "Stopped tracking " + symbol;
    }

    @GetMapping("/tracked")
    public Set<String> getUserTrackedStocks(@AuthenticationPrincipal UserDetails user) {
        String username = user.getUsername();
        log.info("Fetching tracked stocks for user {}", username);
        return stockTrackingService.getUserTrackedStocks(username);
    }
}