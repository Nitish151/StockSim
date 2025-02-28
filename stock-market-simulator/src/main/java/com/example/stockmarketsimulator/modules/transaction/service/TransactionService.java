package com.example.stockmarketsimulator.modules.transaction.service;

import com.example.stockmarketsimulator.modules.portfolio.service.PortfolioService;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.stock.service.StockStorageService;
import com.example.stockmarketsimulator.modules.transaction.dto.TransactionResponse;
import com.example.stockmarketsimulator.modules.transaction.model.Transaction;
import com.example.stockmarketsimulator.modules.transaction.model.Transaction.TransactionType;
import com.example.stockmarketsimulator.modules.transaction.repository.TransactionRepository;
import com.example.stockmarketsimulator.modules.user.model.User;
import com.example.stockmarketsimulator.modules.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final StockStorageService stockStorageService;
    private final PortfolioService portfolioService;

    @Transactional
    public Transaction buyStock(Long userId, String stockSymbol, int quantity) {
        log.info("Processing buy order: User {} buying {} shares of {}", userId, quantity, stockSymbol);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new EntityNotFoundException("User not found");
                });

        Stock stock = stockStorageService.findStockBySymbol(stockSymbol);

        BigDecimal totalCost = stock.getCurrentPrice().multiply(BigDecimal.valueOf(quantity));

        if (user.getBalance().compareTo(totalCost) < 0) {
            log.error("Insufficient balance: User {} has {}, needs {}", userId, user.getBalance(), totalCost);
            throw new IllegalArgumentException("Insufficient balance to buy stock");
        }

        user.setBalance(user.getBalance().subtract(totalCost));
        userRepository.save(user);
        log.info("User {} balance updated: {}", userId, user.getBalance());

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setStock(stock);
        transaction.setType(TransactionType.BUY);
        transaction.setPrice(stock.getCurrentPrice());
        transaction.setTotalPrice(totalCost);
        transaction.setQuantity(quantity);
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);
        log.info("Transaction saved: User {} bought {} shares of {}", userId, quantity, stockSymbol);

        portfolioService.updatePortfolio(user, stock, quantity);
        log.info("Portfolio updated for user {}", userId);

        return transaction;
    }

    @Transactional
    public Transaction sellStock(Long userId, String stockSymbol, int quantity) {
        log.info("Processing sell order: User {} selling {} shares of {}", userId, quantity, stockSymbol);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new EntityNotFoundException("User not found");
                });

        Stock stock = stockStorageService.findStockBySymbol(stockSymbol);

        // TODO: Check if user has the stock and enough quantity after doing portfolio module

        BigDecimal totalEarnings = stock.getCurrentPrice().multiply(BigDecimal.valueOf(quantity));

        user.setBalance(user.getBalance().add(totalEarnings));
        userRepository.save(user);
        log.info("User {} balance updated: {}", userId, user.getBalance());

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setStock(stock);
        transaction.setType(TransactionType.SELL);
        transaction.setPrice(stock.getCurrentPrice());
        transaction.setTotalPrice(totalEarnings);
        transaction.setQuantity(quantity);
        transaction.setTimestamp(LocalDateTime.now());

        transactionRepository.save(transaction);
        log.info("Transaction saved: User {} sold {} shares of {}", userId, quantity, stockSymbol);

        portfolioService.updatePortfolio(user, stock, quantity);
        log.info("Portfolio updated for user {}", userId);

        return transaction;
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .stockSymbol(transaction.getStock().getSymbol()) // Only the stock symbol
                .companyName(transaction.getStock().getCompanyName()) // Only the company name
                .type(transaction.getType())
                .price(transaction.getPrice())
                .totalPrice(transaction.getTotalPrice())
                .quantity(transaction.getQuantity())
                .timestamp(transaction.getTimestamp())
                .build();
    }

    public List<TransactionResponse> getUserTransactions(Long userId) {
        log.info("Fetching transactions for user {}", userId);
        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        // Map Transaction entities to TransactionResponse DTOs
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .toList();
    }
}
