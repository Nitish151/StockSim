package com.example.stockmarketsimulator.modules.stock.controller;

import com.example.stockmarketsimulator.modules.stock.mapper.StockMapper;
import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.stock.service.StockPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Slf4j
public class StockDataController {

    private final StockPersistenceService stockPersistenceService;
    private final StockMapper stockMapper;

    @GetMapping("/{symbol}")
    public ResponseEntity<StockDto> getStock(@PathVariable String symbol) {
        log.info("Fetching stock data for symbol: {}", symbol);
        // Retrieve (and persist/update) the stock data using the new service
        Stock stock = stockPersistenceService.findAndPersistStock(symbol);
        // Convert the entity to DTO for the API response
        StockDto stockDto = stockMapper.toStockDto(stock);
        return ResponseEntity.ok(stockDto);
    }
}
