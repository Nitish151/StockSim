package com.example.stockmarketsimulator.modules.stock.controller;

import com.example.stockmarketsimulator.modules.stock.dto.NewsDto;
import com.example.stockmarketsimulator.modules.stock.dto.NewsResponseDto;
import com.example.stockmarketsimulator.modules.stock.dto.SearchResponseDto;
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

    @GetMapping("/search")
    public ResponseEntity<SearchResponseDto> searchStock(@RequestParam String stockName){
        log.info("Searching for symbol: {}", stockName);
        SearchResponseDto searchResponseDto = stockService.searchStocksByName(stockName);

        return ResponseEntity.ok(searchResponseDto);
    }

    @GetMapping("/news")
    public ResponseEntity<NewsResponseDto> getNews(
            @RequestParam(required = false) String tickers,
            @RequestParam(required = false) String type
    ) {
        NewsResponseDto news = stockService.getNews(tickers, type);
        if (news == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(news);
    }

}