package com.example.stockmarketsimulator.modules.portfolio.service;

import com.example.stockmarketsimulator.modules.portfolio.dto.PortfolioStockDto;
import com.example.stockmarketsimulator.modules.portfolio.dto.PortfolioSummaryDto;
import com.example.stockmarketsimulator.modules.portfolio.model.Portfolio;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.user.model.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PortfolioService {
    PortfolioSummaryDto getUserPortfolio(User user);
    void updatePortfolio(User user, Stock stock, int quanity, BigDecimal buyPrice);

    Optional<Portfolio> findByUserAndStock(User user, Stock stock);
}
