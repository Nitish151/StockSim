package com.example.stockmarketsimulator.modules.service;

import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.stock.repository.StockRepository;
import com.example.stockmarketsimulator.modules.transaction.model.Transaction;
import com.example.stockmarketsimulator.modules.transaction.repository.TransactionRepository;
import com.example.stockmarketsimulator.modules.user.model.User;
import com.example.stockmarketsimulator.modules.user.repository.UserRepository;
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
    public Transaction executeTransaction(Long userId, Long stockId, int quantity, Transaction.TransactionType type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        BigDecimal totalPrice = stock.getCurrentPrice().multiply(BigDecimal.valueOf(quantity));

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setStock(stock);
        transaction.setQuantity(quantity);
        transaction.setPrice(stock.getCurrentPrice());
        transaction.setType(type);
        transaction.setTimestamp(LocalDateTime.now());

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getUserTransactions(Long userId) {
        return transactionRepository.findByUserId(userId);
    }
}
