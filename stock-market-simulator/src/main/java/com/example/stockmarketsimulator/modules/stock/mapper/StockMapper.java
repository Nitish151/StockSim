package com.example.stockmarketsimulator.modules.stock.mapper;

import com.example.stockmarketsimulator.modules.stock.dto.StockDto;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class StockMapper {

    public StockDto toStockDto(JsonNode stockNode) {
        return StockDto.builder()
                .symbol(stockNode.path("symbol").asText())
                .shortName(stockNode.path("shortName").asText("N/A"))
                .longName(stockNode.path("longName").asText("N/A"))
                .exchange(stockNode.path("fullExchangeName").asText("Unknown"))
                .marketState(stockNode.path("marketState").asText("Unknown"))
                .regularMarketPrice(getBigDecimal(stockNode, "regularMarketPrice"))
                .regularMarketChange(getBigDecimal(stockNode, "regularMarketChange"))
                .regularMarketChangePercent(getBigDecimal(stockNode, "regularMarketChangePercent"))
                .regularMarketPreviousClose(getBigDecimal(stockNode, "regularMarketPreviousClose"))
                .regularMarketOpen(getBigDecimal(stockNode, "regularMarketOpen"))
                .regularMarketDayHigh(getBigDecimal(stockNode, "regularMarketDayHigh"))
                .regularMarketDayLow(getBigDecimal(stockNode, "regularMarketDayLow"))
                .regularMarketVolume(stockNode.path("regularMarketVolume").asLong(0L))
                .marketCap(getBigDecimal(stockNode, "marketCap"))
                .fiftyTwoWeekRange(stockNode.path("fiftyTwoWeekRange").asText("N/A"))
                .fiftyTwoWeekHigh(getBigDecimal(stockNode, "fiftyTwoWeekHigh"))
                .fiftyTwoWeekLow(getBigDecimal(stockNode, "fiftyTwoWeekLow"))
                .fiftyTwoWeekHighChangePercent(getBigDecimal(stockNode, "fiftyTwoWeekHighChangePercent"))
                .fiftyDayAverage(getBigDecimal(stockNode, "fiftyDayAverage"))
                .twoHundredDayAverage(getBigDecimal(stockNode, "twoHundredDayAverage"))
                .trailingPE(getBigDecimal(stockNode, "trailingPE"))
                .forwardPE(getBigDecimal(stockNode, "forwardPE"))
                .epsTrailingTwelveMonths(getBigDecimal(stockNode, "epsTrailingTwelveMonths"))
                .dividendYield(getBigDecimal(stockNode, "dividendYield"))
                .priceToBook(getBigDecimal(stockNode, "priceToBook"))
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    public StockDto toStockDto(Stock stock) {
        return StockDto.builder()
                .symbol(stock.getSymbol())
                .shortName(stock.getCompanyName())
                .longName(stock.getCompanyName())
                .exchange("Unknown")
                .marketState("Unknown")
                .regularMarketPrice(stock.getCurrentPrice())
                .regularMarketOpen(stock.getOpeningPrice())
                .regularMarketPreviousClose(stock.getPreviousClose())
                .regularMarketVolume(stock.getVolume())
                .marketCap(stock.getMarketCap())
                .regularMarketChange(stock.getPriceChange())
                .regularMarketChangePercent(stock.getPercentageChange())
                .fiftyTwoWeekRange("N/A")
                .fiftyTwoWeekHigh(BigDecimal.ZERO)
                .fiftyTwoWeekLow(BigDecimal.ZERO)
                .fiftyTwoWeekHighChangePercent(BigDecimal.ZERO)
                .fiftyDayAverage(BigDecimal.ZERO)
                .twoHundredDayAverage(BigDecimal.ZERO)
                .trailingPE(BigDecimal.ZERO)
                .forwardPE(BigDecimal.ZERO)
                .epsTrailingTwelveMonths(BigDecimal.ZERO)
                .dividendYield(BigDecimal.ZERO)
                .priceToBook(BigDecimal.ZERO)
                .lastUpdated(stock.getLastUpdated())
                .build();
    }

    public Stock toStockEntity(StockDto dto) {
        return Stock.builder()
                .symbol(dto.getSymbol())
                .companyName(dto.getLongName())
                .industry("Unknown") // Default value; adjust as needed
                .currentPrice(dto.getRegularMarketPrice())
                .openingPrice(dto.getRegularMarketOpen())
                .previousClose(dto.getRegularMarketPreviousClose())
                .volume(dto.getRegularMarketVolume())
                .marketCap(dto.getMarketCap())
                .priceChange(dto.getRegularMarketChange())
                .percentageChange(dto.getRegularMarketChangePercent())
                .lastUpdated(dto.getLastUpdated())
                .build();
    }

    private BigDecimal getBigDecimal(JsonNode node, String field) {
        return node.has(field) && node.get(field).isNumber()
                ? node.get(field).decimalValue()
                : BigDecimal.ZERO;
    }
}