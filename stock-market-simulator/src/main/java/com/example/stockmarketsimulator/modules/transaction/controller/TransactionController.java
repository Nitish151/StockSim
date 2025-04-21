
// TransactionController.java
package com.example.stockmarketsimulator.modules.transaction.controller;

import com.example.stockmarketsimulator.modules.transaction.dto.TransactionRequest;
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

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {
    private final TransactionService transactionService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody TransactionRequest request) {

        // Fetch the user from the repository
        String username = userDetails.getUsername();
        User user = userService.searchUserByUsernameOrEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("User {} is attempting to {} {} shares of {}",
                user.getId(),
                request.getType(),
                request.getQuantity(),
                request.getStockSymbol());

        try {
            Transaction transaction = transactionService.executeTransaction(
                    user.getId(),
                    request.getStockSymbol(),
                    request.getQuantity(),
                    request.getType());

            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Error processing {} transaction: {}", request.getType(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<TransactionResponse>> getUserTransactions(
            @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        User user = userService.searchUserByUsernameOrEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Fetching transaction history for user {}", user.getId());
        return ResponseEntity.ok(transactionService.getUserTransactions(user.getId()));
    }
}