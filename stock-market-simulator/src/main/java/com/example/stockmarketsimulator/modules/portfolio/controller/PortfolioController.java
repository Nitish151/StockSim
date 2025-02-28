package com.example.stockmarketsimulator.modules.portfolio.controller;

import com.example.stockmarketsimulator.modules.portfolio.model.Portfolio;
import com.example.stockmarketsimulator.modules.portfolio.service.PortfolioService;
import com.example.stockmarketsimulator.modules.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {
    private final PortfolioService portfolioService;

    @GetMapping
    public ResponseEntity<List<Portfolio>> getUserPortfolio(@AuthenticationPrincipal User user){
        return ResponseEntity.ok(portfolioService.getUserPortfolio(user));
    }
}
