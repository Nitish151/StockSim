package com.example.stockmarketsimulator.modules.portfolio.repository;

import com.example.stockmarketsimulator.modules.portfolio.model.Portfolio;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUser(User user);
    Optional<Portfolio> findByUserAndStock(User user, Stock stock);
}
