package com.example.stockmarketsimulator.modules.portfolio.service;

import com.example.stockmarketsimulator.modules.portfolio.model.Portfolio;
import com.example.stockmarketsimulator.modules.portfolio.repository.PortfolioRepository;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.user.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioServiceImpl implements PortfolioService{
    private final PortfolioRepository portfolioRepository;

    @Override
    public List<Portfolio> getUserPortfolio(User user){
        return portfolioRepository.findByUser(user);
    }

    @Transactional
    @Override
    public void updatePortfolio(User user, Stock stock, int quantity){
        Portfolio portfolio = portfolioRepository.findByUserAndStock(user, stock)
                .orElse(new Portfolio(null, user, stock, 0));

        int updatedQuantity = portfolio.getQuantity() + quantity;
        if(updatedQuantity <= 0){
            portfolioRepository.delete(portfolio);
        } else {
            portfolio.setQuantity(updatedQuantity);
            portfolioRepository.save(portfolio);
        }

    }
}
