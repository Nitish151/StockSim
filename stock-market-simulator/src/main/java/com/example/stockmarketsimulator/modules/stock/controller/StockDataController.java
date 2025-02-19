package com.example.stockmarketsimulator.modules.stock.controller;

import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
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
     * Endpoint to fetch stock from stock symbol.
     * URL: GET /api/stocks/{symbol}
     */
    @GetMapping("/{symbol}")
    public ResponseEntity<StockDto> getStock(@PathVariable String symbol) {
        log.info("📊 Fetching stock %s", symbol);
        StockDto stockDto = stockDataService.fetchStockData(symbol);
        return ResponseEntity.ok(stockDto);
    }
}
