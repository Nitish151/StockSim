package com.example.stockmarketsimulator.modules.portfolio.service;

import com.example.stockmarketsimulator.modules.portfolio.dto.PortfolioResponse;
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
    public List<PortfolioResponse> getUserPortfolio(User user){
        List<Portfolio> portfolios = portfolioRepository.findByUser(user);

        return portfolios.stream()
                .map(this::mapToPortfolioResponse)
                .toList();
    }

    private PortfolioResponse mapToPortfolioResponse(Portfolio portfolio) {
        Stock stock = portfolio.getStock();

        // Calculated derived fields

        BigDecimal totalInvestment = portfolio.getAvgBuyPrice().multiply(BigDecimal.valueOf(portfolio.getQuantity()));
        BigDecimal currentValue = stock.getCurrentPrice().multiply(BigDecimal.valueOf(portfolio.getQuantity()));
        BigDecimal profitOrLoss = currentValue.subtract(totalInvestment);

        return PortfolioResponse.builder()
                .id(portfolio.getId())
                .stockSymbol(stock.getSymbol()) // Only the stock symbol
                .companyName(stock.getCompanyName()) // Only the company name
                .currentPrice(stock.getCurrentPrice()) // Current price of the stock
                .quantity(portfolio.getQuantity()) // Number of shares held
                .avgBuyPrice(portfolio.getAvgBuyPrice()) // Average buy price
                .totalInvestment(totalInvestment) // Total investment
                .currentValue(currentValue) // Current value of the holding
                .profitOrLoss(profitOrLoss) // Profit or loss
                .build();
    }

    @Transactional
    @Override
    public void updatePortfolio(User user, Stock stock, int quantity, BigDecimal buyPrice) {
        Portfolio portfolio = portfolioRepository.findByUserAndStock(user, stock)
                .orElse(new Portfolio(null, user, stock, 0, BigDecimal.ZERO));

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
