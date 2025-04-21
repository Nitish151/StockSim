package com.example.stockmarketsimulator.modules.user.controller;

import com.example.stockmarketsimulator.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final UserService userService;

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestParam BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Amount must be greater than 0");
        }

        userService.deposit(userDetails.getUsername(), amount);
        return ResponseEntity.ok("Deposit successful");
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestParam BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body("Amount must be greater than 0");
        }

        boolean success = userService.withdraw(userDetails.getUsername(), amount);
        return success ?
                ResponseEntity.ok("Withdrawal successful") :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient balance");
    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getBalance(@AuthenticationPrincipal UserDetails userDetails) {
        BigDecimal balance = userService.getUserBalance(userDetails.getUsername());
        return ResponseEntity.ok(balance);
    }
}
