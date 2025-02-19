package com.example.stockmarketsimulator.modules.controller;

import com.example.stockmarketsimulator.modules.service.TransactionService;
import com.example.stockmarketsimulator.modules.transaction.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/execute")
    public ResponseEntity<Transaction> executeTransaction(
            @RequestParam Long userId,
            @RequestParam Long stockId,
            @RequestParam int quantity,
            @RequestParam Transaction.TransactionType type) {

        Transaction transaction = transactionService.executeTransaction(userId, stockId, quantity, type);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable Long userId) {
        List<Transaction> transactions = transactionService.getUserTransactions(userId);
        return ResponseEntity.ok(transactions);
    }
}
