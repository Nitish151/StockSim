package com.example.stockmarketsimulator.modules.portfolio.controller;

import com.example.stockmarketsimulator.modules.portfolio.dto.PortfolioStockDto;
import com.example.stockmarketsimulator.modules.portfolio.dto.PortfolioSummaryDto;
import com.example.stockmarketsimulator.modules.portfolio.model.Portfolio;
import com.example.stockmarketsimulator.modules.portfolio.service.PortfolioService;
import com.example.stockmarketsimulator.modules.user.model.User;
import com.example.stockmarketsimulator.modules.user.repository.UserRepository;
import com.example.stockmarketsimulator.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
@Slf4j
public class PortfolioController {
    private final PortfolioService portfolioService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<PortfolioSummaryDto> getUserPortfolio(@AuthenticationPrincipal UserDetails userDetails) {
        // Fetch the user from the repository using the username
        log.info("⬇️ Here");
        Optional<User> userOptional = userService.searchUserByUsername(userDetails.getUsername());

        // Check if the user exists
        if (userOptional.isEmpty()) {
            log.error("User not found: {}", userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Get the user from the Optional
        User user = userOptional.get();

        // Fetch and return the user's portfolio
        return ResponseEntity.ok(portfolioService.getUserPortfolio(user));
    }

}
