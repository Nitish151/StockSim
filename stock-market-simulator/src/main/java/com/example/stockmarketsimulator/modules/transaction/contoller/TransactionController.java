package com.example.stockmarketsimulator.modules.transaction.controller;

import com.example.stockmarketsimulator.modules.transaction.dto.TransactionResponse;
import com.example.stockmarketsimulator.modules.transaction.model.Transaction;
import com.example.stockmarketsimulator.modules.transaction.service.TransactionService;
import com.example.stockmarketsimulator.modules.user.model.User;
import com.example.stockmarketsimulator.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;
    private final UserService userService;

    @PostMapping("/buy")
    public ResponseEntity<Transaction> buyStock(
            @AuthenticationPrincipal UserDetails user1, // Inject username/email
            @RequestParam String stockSymbol,
            @RequestParam int quantity) {

        log.info("here😭");
        // Fetch the user from the repository
        String username = user1.getUsername();
        User user = userService.searchUserByUsernameOrEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("User {} is attempting to buy {} shares of {}", user.getId(), quantity, stockSymbol);

        try {
            Transaction transaction = transactionService.buyStock(user.getId(), stockSymbol, quantity);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Error processing buy transaction: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<Transaction> sellStock(
            @AuthenticationPrincipal UserDetails user1,
            @RequestParam String stockSymbol,
            @RequestParam int quantity) {
        String username = user1.getUsername();
        User user = userService.searchUserByUsernameOrEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("✅ User {} is attempting to sell {} shares of {}", user.getId(), quantity, stockSymbol);

        try {
            Transaction transaction = transactionService.sellStock(user.getId(), stockSymbol, quantity);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Error processing sell transaction: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(
            @AuthenticationPrincipal UserDetails user1) {
        String username = user1.getUsername();
        User user = userService.searchUserByUsernameOrEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Fetching transaction history for user {}", user.getId());
        return ResponseEntity.ok(transactionService.getUserTransactions(user.getId()));
    }
}
