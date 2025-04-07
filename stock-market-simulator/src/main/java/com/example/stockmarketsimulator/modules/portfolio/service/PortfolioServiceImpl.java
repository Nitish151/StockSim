package com.example.stockmarketsimulator.modules.portfolio.service;

import com.example.stockmarketsimulator.modules.portfolio.dto.PortfolioStockDto;
import com.example.stockmarketsimulator.modules.portfolio.dto.PortfolioSummaryDto;
import com.example.stockmarketsimulator.modules.portfolio.model.Portfolio;
import com.example.stockmarketsimulator.modules.portfolio.repository.PortfolioRepository;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.user.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService{
    private final PortfolioRepository portfolioRepository;

    @Override
    public PortfolioSummaryDto getUserPortfolio(User user) {
        List<Portfolio> portfolios = portfolioRepository.findByUser(user);

        List<PortfolioStockDto> stockDtos = portfolios.stream()
                .map(this::mapToPortfolioStockDto)
                .toList();

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        int totalStocksHeld = 0;

        for (PortfolioStockDto stock : stockDtos) {
            totalInvested = totalInvested.add(stock.getTotalInvestment());
            currentValue = currentValue.add(stock.getCurrentValue());
            totalStocksHeld += stock.getQuantity();
        }

        BigDecimal totalProfitOrLoss = currentValue.subtract(totalInvested);

        BigDecimal availableBalance = user.getBalance(); // or get from a wallet service

        return PortfolioSummaryDto.builder()
                .totalInvested(totalInvested)
                .currentValue(currentValue)
                .totalProfitOrLoss(totalProfitOrLoss)
                .availableBalance(availableBalance)
                .totalStocksHeld(totalStocksHeld)
                .holdings(stockDtos)
                .build();
    }

    private PortfolioStockDto mapToPortfolioStockDto(Portfolio portfolio) {
        Stock stock = portfolio.getStock();

        BigDecimal totalInvestment = portfolio.getAvgBuyPrice()
                .multiply(BigDecimal.valueOf(portfolio.getQuantity()));

        BigDecimal currentValue = stock.getCurrentPrice()
                .multiply(BigDecimal.valueOf(portfolio.getQuantity()));

        BigDecimal profitOrLoss = currentValue.subtract(totalInvestment);

        return PortfolioStockDto.builder()
                .id(portfolio.getId())
                .stockSymbol(stock.getSymbol())
                .companyName(stock.getCompanyName())
                .currentPrice(stock.getCurrentPrice())
                .quantity(portfolio.getQuantity())
                .avgBuyPrice(portfolio.getAvgBuyPrice())
                .totalInvestment(totalInvestment)
                .currentValue(currentValue)
                .profitOrLoss(profitOrLoss)
                .build();
    }

    @Transactional
    @Override
    public void updatePortfolio(User user, Stock stock, int quantity, BigDecimal buyPrice) {
        Portfolio portfolio = portfolioRepository.findByUserAndStock(user, stock)
                .orElse(Portfolio.builder()
                        .user(user)
                        .stock(stock)
                        .quantity(0)
                        .avgBuyPrice(BigDecimal.ZERO)
                        .build());

        int updatedQuantity = portfolio.getQuantity() + quantity;

        if (updatedQuantity <= 0) {
            portfolioRepository.delete(portfolio);
        } else {
            // Calculate the new average buy price
            BigDecimal totalCost = buyPrice.multiply(BigDecimal.valueOf(quantity));
            BigDecimal currentTotalCost = portfolio.getAvgBuyPrice().multiply(BigDecimal.valueOf(portfolio.getQuantity()));
            BigDecimal newTotalCost = currentTotalCost.add(totalCost);
            BigDecimal newAvgBuyPrice = newTotalCost.divide(BigDecimal.valueOf(updatedQuantity), 2, RoundingMode.HALF_UP);

            portfolio.setQuantity(updatedQuantity);
            portfolio.setAvgBuyPrice(newAvgBuyPrice);
            portfolioRepository.save(portfolio);
        }
    }

    public Optional<Portfolio> findByUserAndStock(User user, Stock stock) {
        return portfolioRepository.findByUserAndStock(user, stock);
    }

}
