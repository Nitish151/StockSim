// TransactionService.java
package com.example.stockmarketsimulator.modules.transaction.service;

import com.example.stockmarketsimulator.modules.portfolio.model.Portfolio;
import com.example.stockmarketsimulator.modules.portfolio.service.PortfolioService;
import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.stock.service.StockService;
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
    private final StockService stockService;
    private final PortfolioService portfolioService;

    @Transactional
    public Transaction executeTransaction(Long userId, String stockSymbol, int quantity, TransactionType type) {
        log.info("Processing {} order: User {} for {} shares of {}", type, userId, quantity, stockSymbol);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new EntityNotFoundException("User not found");
                });

        // Fetch and persist the stock
        Stock stock = stockService.getAndPersistStock(stockSymbol);

        switch (type) {
            case BUY:
                return processBuyTransaction(user, stock, quantity);
            case SELL:
                return processSellTransaction(user, stock, quantity);
            default:
                throw new IllegalArgumentException("Unsupported transaction type: " + type);
        }
    }

    private Transaction processBuyTransaction(User user, Stock stock, int quantity) {
        BigDecimal totalCost = stock.getCurrentPrice().multiply(BigDecimal.valueOf(quantity));

        if (user.getBalance().compareTo(totalCost) < 0) {
            log.error("Insufficient balance: User {} has {}, needs {}",
                    user.getId(), user.getBalance(), totalCost);
            throw new IllegalArgumentException("Insufficient balance to buy stock");
        }

        user.setBalance(user.getBalance().subtract(totalCost));
        userRepository.save(user);
        log.info("User {} balance updated: {}", user.getId(), user.getBalance());

        Transaction transaction = createTransaction(user, stock, TransactionType.BUY,
                stock.getCurrentPrice(), totalCost, quantity, BigDecimal.ZERO);

        portfolioService.updatePortfolio(user, stock, quantity, stock.getCurrentPrice());
        log.info("Portfolio updated for user {}", user.getId());

        return transaction;
    }

    private Transaction processSellTransaction(User user, Stock stock, int quantity) {
        Portfolio portfolio = portfolioService.findByUserAndStock(user, stock)
                .orElseThrow(() -> {
                    log.error("User {} does not own stock {}", user.getId(), stock.getSymbol());
                    return new IllegalArgumentException("User does not own this stock");
                });

        if (portfolio.getQuantity() < quantity) {
            log.error("Insufficient shares: User {} has {} shares of {}, trying to sell {}",
                    user.getId(), portfolio.getQuantity(), stock.getSymbol(), quantity);
            throw new IllegalArgumentException("Insufficient shares to sell");
        }

        BigDecimal totalEarnings = stock.getCurrentPrice().multiply(BigDecimal.valueOf(quantity));

        user.setBalance(user.getBalance().add(totalEarnings));
        userRepository.save(user);
        log.info("User {} balance updated: {}", user.getId(), user.getBalance());
        BigDecimal avgBuyPrice = portfolio.getAvgBuyPrice();
        BigDecimal sellPrice = stock.getCurrentPrice();
        BigDecimal profitPerShare = sellPrice.subtract(avgBuyPrice);
        BigDecimal totalProfit = profitPerShare.multiply(BigDecimal.valueOf(quantity));

        Transaction transaction = createTransaction(user, stock, TransactionType.SELL,
                sellPrice, totalEarnings, quantity, totalProfit);



        portfolioService.updatePortfolio(user, stock, -quantity, stock.getCurrentPrice());
        log.info("Portfolio updated for user {}", user.getId());

        return transaction;
    }

    private Transaction createTransaction(User user, Stock stock, TransactionType type,
                                          BigDecimal price, BigDecimal totalPrice,
                                          int quantity, BigDecimal profitOrLoss) {
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setStock(stock);
        transaction.setType(type);
        transaction.setPrice(price);
        transaction.setTotalPrice(totalPrice);
        transaction.setQuantity(quantity);
        transaction.setTimestamp(LocalDateTime.now());
        if (type == TransactionType.SELL && (profitOrLoss != null || profitOrLoss != BigDecimal.ZERO)) {
            transaction.setProfitOrLoss(profitOrLoss);
        }
        return transactionRepository.save(transaction);
    }

    public List<TransactionResponse> getUserTransactions(Long userId) {
        log.info("Fetching transactions for user {}", userId);
        List<Transaction> transactions = transactionRepository.findByUserId(userId);

        // Map Transaction entities to TransactionResponse DTOs
        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .toList();
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .stockSymbol(transaction.getStock().getSymbol())
                .companyName(transaction.getStock().getCompanyName())
                .type(transaction.getType())
                .price(transaction.getPrice())
                .totalPrice(transaction.getTotalPrice())
                .quantity(transaction.getQuantity())
                .timestamp(transaction.getTimestamp())
                .build();
    }
}