package com.example.stockmarketsimulator.modules.stock.controller;

import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.example.stockmarketsimulator.modules.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Slf4j
public class StockDataController {

    private final StockService stockService;

    @GetMapping("/{symbol}")
    public ResponseEntity<StockDto> getStock(@PathVariable String symbol) {
        log.info("Fetching stock data for symbol: {}", symbol);
        StockDto stockDto = stockService.getStockData(symbol);
        return ResponseEntity.ok(stockDto);
    }
}