package com.example.stockmarketsimulator.modules.transaction.controller;


import com.example.stockmarketsimulator.modules.transaction.dto.LimitOrderRequest;
import com.example.stockmarketsimulator.modules.transaction.dto.LimitOrderResponse;
import com.example.stockmarketsimulator.modules.transaction.model.LimitOrder;
import com.example.stockmarketsimulator.modules.transaction.service.LimitOrderService;
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
@RequestMapping("/api/limit-orders")
@RequiredArgsConstructor
@Slf4j
public class LimitOrderController {
    private final LimitOrderService limitOrderService;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<LimitOrderResponse> createLimitOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody LimitOrderRequest request) {

        String username = userDetails.getUsername();
        User user = userService.searchUserByUsernameOrEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("User {} is creating a {} limit order for {} shares of {} at price {}",
                user.getId(),
                request.getType(),
                request.getQuantity(),
                request.getStockSymbol(),
                request.getLimitPrice());

        try {
            LimitOrder limitOrder = limitOrderService.createLimitOrder(request, user.getId());
            return ResponseEntity.ok(convertToResponse(limitOrder));
        } catch (Exception e) {
            log.error("Error creating limit order: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> cancelLimitOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {

        String username = userDetails.getUsername();
        User user = userService.searchUserByUsernameOrEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("User {} is canceling limit order {}", user.getId(), orderId);

        try {
            boolean canceled = limitOrderService.cancelLimitOrder(orderId, user.getId());
            if (canceled) {
                return ResponseEntity.ok("Limit order canceled successfully");
            } else {
                return ResponseEntity.badRequest().body("Could not cancel the limit order");
            }
        } catch (Exception e) {
            log.error("Error canceling limit order: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<LimitOrderResponse>> getAllLimitOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User user = userService.searchUserByUsernameOrEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Fetching all limit orders for user {}", user.getId());
        List<LimitOrderResponse> orders = limitOrderService.getUserLimitOrders(user.getId());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<LimitOrderResponse>> getPendingLimitOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();
        User user = userService.searchUserByUsernameOrEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Fetching pending limit orders for user {}", user.getId());
        List<LimitOrderResponse> orders = limitOrderService.getUserPendingLimitOrders(user.getId());
        return ResponseEntity.ok(orders);
    }

    private LimitOrderResponse convertToResponse(LimitOrder limitOrder) {
        return LimitOrderResponse.builder()
                .id(limitOrder.getId())
                .stockSymbol(limitOrder.getStock().getSymbol())
                .companyName(limitOrder.getStock().getCompanyName())
                .type(limitOrder.getType())
                .limitPrice(limitOrder.getLimitPrice())
                .quantity(limitOrder.getQuantity())
                .status(String.valueOf(limitOrder.getStatus()))
                .createdAt(limitOrder.getCreatedAt())
                .expiresAt(limitOrder.getExpiresAt())
                .build();
    }
}