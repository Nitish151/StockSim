package com.example.stockmarketsimulator.modules.transaction.service;

import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.stock.repository.StockRepository;
import com.example.stockmarketsimulator.modules.transaction.model.Transaction;
import com.example.stockmarketsimulator.modules.transaction.model.Transaction.TransactionType;
import com.example.stockmarketsimulator.modules.transaction.repository.TransactionRepository;
import com.example.stockmarketsimulator.modules.user.model.User;
import com.example.stockmarketsimulator.modules.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    @Transactional
    public Transaction buyStock(Long userId, String stockSymbol, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Stock stock = stockRepository.findBySymbol(stockSymbol)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found"));

        BigDecimal totalCost = stock.getCurrentPrice().multiply(BigDecimal.valueOf(quantity));

        // Check if user has enough balance
        if (user.getBalance().compareTo(totalCost) < 0) {
            throw new IllegalArgumentException("Insufficient balance to buy stock");
        }

        // Deduct balance and save user
        user.setBalance(user.getBalance().subtract(totalCost));
        userRepository.save(user);

        // Create and save transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setStock(stock);
        transaction.setType(TransactionType.BUY);
        transaction.setPrice(stock.getCurrentPrice());
        transaction.setQuantity(quantity);
        transaction.setTimestamp(LocalDateTime.now());

        // TODO: Store in portfolio

        return transactionRepository.save(transaction);
    }

    @Transactional
    public Transaction sellStock(Long userId, String stockSymbol, int quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Stock stock = stockRepository.findBySymbol(stockSymbol)
                .orElseThrow(() -> new EntityNotFoundException("Stock not found"));

        // TODO: Check if user owns the stock (in the portfolio)

        BigDecimal totalEarnings = stock.getCurrentPrice().multiply(BigDecimal.valueOf(quantity));

        // Add balance and save user
        user.setBalance(user.getBalance().add(totalEarnings));
        userRepository.save(user);

        // Create and save transaction
        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setStock(stock);
        transaction.setType(TransactionType.SELL);
        transaction.setPrice(stock.getCurrentPrice());
        transaction.setQuantity(quantity);
        transaction.setTimestamp(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getUserTransactions(Long userId){
        return transactionRepository.findByUserId(userId);
    }
}

