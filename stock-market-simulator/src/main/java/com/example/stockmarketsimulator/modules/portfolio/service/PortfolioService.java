package com.example.stockmarketsimulator.modules.portfolio.service;

import com.example.stockmarketsimulator.modules.portfolio.dto.PortfolioResponse;
import com.example.stockmarketsimulator.modules.portfolio.model.Portfolio;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.user.model.User;

import java.math.BigDecimal;
import java.util.List;

public interface PortfolioService {
    List<PortfolioResponse> getUserPortfolio(User user);
    void updatePortfolio(User user, Stock stock, int quanity, BigDecimal buyPrice);
}
