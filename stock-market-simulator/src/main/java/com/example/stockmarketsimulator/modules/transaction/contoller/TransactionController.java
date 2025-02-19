package com.example.stockmarketsimulator.modules.transaction.contoller;

import com.example.stockmarketsimulator.modules.transaction.model.Transaction;
import com.example.stockmarketsimulator.modules.transaction.service.TransactionService;
import com.example.stockmarketsimulator.modules.user.model.User;
import com.example.stockmarketsimulator.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;

    @PostMapping("/buy")
    public ResponseEntity<Transaction> buyStock(
            @AuthenticationPrincipal User user,
            @RequestParam String stockSymbol,
            @RequestParam int quantity) {

        Transaction transaction = transactionService.buyStock(user.getId(), stockSymbol, quantity);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Transaction>> getUserTransactions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(transactionService.getUserTransactions(user.getId()));
    }

    @PostMapping("/sell")
    public ResponseEntity<Transaction> sellStock(
            @AuthenticationPrincipal User user,
            @RequestParam String stockSymbol,
            @RequestParam int quantity){

        Transaction transaction = transactionService.sellStock(user.getId(), stockSymbol, quantity);
        return ResponseEntity.ok(transaction);
    }
}
