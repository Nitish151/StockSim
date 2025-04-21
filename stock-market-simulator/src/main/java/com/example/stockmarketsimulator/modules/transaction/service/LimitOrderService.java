package com.example.stockmarketsimulator.modules.transaction.service;

import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.stock.service.StockService;
import com.example.stockmarketsimulator.modules.transaction.dto.LimitOrderRequest;
import com.example.stockmarketsimulator.modules.transaction.dto.LimitOrderResponse;
import com.example.stockmarketsimulator.modules.transaction.model.LimitOrder;
import com.example.stockmarketsimulator.modules.transaction.model.Transaction;
import com.example.stockmarketsimulator.modules.transaction.repository.LimitOrderRepository;
import com.example.stockmarketsimulator.modules.user.model.User;
import com.example.stockmarketsimulator.modules.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LimitOrderService {
    private final LimitOrderRepository limitOrderRepository;
    private final UserRepository userRepository;
    private final StockService stockService;
    private final TransactionService transactionService;

    @Transactional
    public LimitOrder createLimitOrder(LimitOrderRequest request, Long userId) {
        log.info("Creating {} limit order: User {} for {} shares of {} at price {}",
                request.getType(), userId, request.getQuantity(), request.getStockSymbol(), request.getLimitPrice());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new EntityNotFoundException("User not found");
                });

        Stock stock = stockService.getAndPersistStock(request.getStockSymbol());

        // Validate limit order
        validateLimitOrder(user, stock, request);

        // For buy orders, reserve funds
        if (request.getType() == Transaction.TransactionType.BUY) {
            reserveFunds(user, request.getLimitPrice(), request.getQuantity());
        }

        // Create and save the limit order
        LimitOrder limitOrder = LimitOrder.builder()
                .user(user)
                .stock(stock)
                .type(request.getType())
                .limitPrice(request.getLimitPrice())
                .quantity(request.getQuantity())
                .createdAt(LocalDateTime.now())
                .expiresAt(request.getExpiresAt())
                .status(LimitOrder.LimitOrderStatus.PENDING)
                .build();

        return limitOrderRepository.save(limitOrder);
    }

    private void validateLimitOrder(User user, Stock stock, LimitOrderRequest request) {
        // For buy orders, check if user has enough balance
        if (request.getType() == Transaction.TransactionType.BUY) {
            BigDecimal totalCost = request.getLimitPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
            if (user.getBalance().compareTo(totalCost) < 0) {
                log.error("Insufficient balance: User {} has {}, needs {}",
                        user.getId(), user.getBalance(), totalCost);
                throw new IllegalArgumentException("Insufficient balance to place buy limit order");
            }
        }
        // For sell orders, check if user has enough shares
        else if (request.getType() == Transaction.TransactionType.SELL) {
            boolean hasEnoughShares = transactionService.userHasEnoughShares(user.getId(), stock.getId(), request.getQuantity());
            if (!hasEnoughShares) {
                log.error("Insufficient shares: User {} doesn't have enough shares of {}",
                        user.getId(), stock.getSymbol());
                throw new IllegalArgumentException("Insufficient shares to place sell limit order");
            }
        }
    }

    private void reserveFunds(User user, BigDecimal price, int quantity) {
        BigDecimal totalCost = price.multiply(BigDecimal.valueOf(quantity));
        user.setBalance(user.getBalance().subtract(totalCost));
        user.setReservedBalance(user.getReservedBalance().add(totalCost));
        userRepository.save(user);
        log.info("Reserved {} from user {}'s balance for limit order", totalCost, user.getId());
    }

    private void releaseFunds(User user, BigDecimal price, int quantity) {
        BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(quantity));
        user.setReservedBalance(user.getReservedBalance().subtract(totalAmount));
        user.setBalance(user.getBalance().add(totalAmount));
        userRepository.save(user);
        log.info("Released {} back to user {}'s balance from canceled limit order", totalAmount, user.getId());
    }

    @Transactional
    public boolean cancelLimitOrder(Long orderId, Long userId) {
        LimitOrder order = limitOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Limit order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to cancel this order");
        }

        if (order.getStatus() != LimitOrder.LimitOrderStatus.PENDING) {
            throw new IllegalArgumentException("Only pending orders can be canceled");
        }

        // Release reserved funds for buy orders
        if (order.getType() == Transaction.TransactionType.BUY) {
            releaseFunds(order.getUser(), order.getLimitPrice(), order.getQuantity());
        }

        order.setStatus(LimitOrder.LimitOrderStatus.CANCELED);
        limitOrderRepository.save(order);
        log.info("Limit order {} canceled by user {}", orderId, userId);
        return true;
    }

    @Scheduled(fixedRate = 600000) // Run every minute
    @Transactional
    public void checkAndExecuteLimitOrders() {
        log.info("Running scheduled check for limit orders to execute");

        // Get all active stocks with pending limit orders
        List<Stock> stocksWithPendingOrders = stockService.getStocksWithPendingLimitOrders();

        for (Stock stock : stocksWithPendingOrders) {
            // Update stock price
            Stock updatedStock = stockService.refreshStockPrice(stock.getSymbol());
            BigDecimal currentPrice = updatedStock.getCurrentPrice();

            // Find buy orders to execute (current price <= limit price)
            List<LimitOrder> buyOrdersToExecute =
                    limitOrderRepository.findBuyOrdersToExecute(updatedStock.getId(), currentPrice);

            // Find sell orders to execute (current price >= limit price)
            List<LimitOrder> sellOrdersToExecute =
                    limitOrderRepository.findSellOrdersToExecute(updatedStock.getId(), currentPrice);

            // Execute buy orders
            for (LimitOrder order : buyOrdersToExecute) {
                try {
                    executeLimitOrder(order, currentPrice);
                    log.info("Executed BUY limit order ID {} for user {} at price {}",
                            order.getId(), order.getUser().getId(), currentPrice);

                } catch (Exception e) {
                    log.error("Error executing buy limit order {}: {}", order.getId(), e.getMessage(), e);
                }

            }

            // Execute sell orders
            for (LimitOrder order : sellOrdersToExecute) {
                try {
                    executeLimitOrder(order, currentPrice);
                    log.info("Executed SELL limit order ID {} for user {} at price {}",
                            order.getId(), order.getUser().getId(), currentPrice);

                } catch (Exception e) {
                    log.error("Error executing sell limit order {}: {}", order.getId(), e.getMessage(), e);
                }
            }
        }

        // Handle expired orders
        expireOldOrders();
    }

    @Transactional
    public void executeLimitOrder(LimitOrder order, BigDecimal currentPrice) {
        log.info("Executing limit order {}: {} {} shares of {} at limit price {}, current price {}",
                order.getId(), order.getType(), order.getQuantity(),
                order.getStock().getSymbol(), order.getLimitPrice(), currentPrice);

        // For buy orders, release reserved funds and use actual price
        if (order.getType() == Transaction.TransactionType.BUY) {
            User user = order.getUser();
            BigDecimal reservedAmount = order.getLimitPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
            user.setReservedBalance(user.getReservedBalance().subtract(reservedAmount));
            userRepository.save(user);
        }

        // Execute the transaction
        Transaction transaction = transactionService.executeTransactionWithOrderType(
                order.getUser().getId(),
                order.getStock().getId(),
                order.getQuantity(),
                order.getType(),
                Transaction.OrderType.LIMIT,
                order.getLimitPrice()
        );

        // Update limit order status
        order.setStatus(LimitOrder.LimitOrderStatus.EXECUTED);
        limitOrderRepository.save(order);

        log.info("Successfully executed limit order {}, created transaction {}",
                order.getId(), transaction.getId());
    }

    @Transactional
    public void expireOldOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<LimitOrder> expiredOrders =
                limitOrderRepository.findByStatusAndExpiresAtBefore(LimitOrder.LimitOrderStatus.PENDING, now);

        for (LimitOrder order : expiredOrders) {
            log.info("Expiring limit order {}: {} {} shares of {} at {}",
                    order.getId(), order.getType(), order.getQuantity(),
                    order.getStock().getSymbol(), order.getLimitPrice());

            // Release reserved funds for buy orders
            if (order.getType() == Transaction.TransactionType.BUY) {
                releaseFunds(order.getUser(), order.getLimitPrice(), order.getQuantity());
            }

            order.setStatus(LimitOrder.LimitOrderStatus.EXPIRED);
            limitOrderRepository.save(order);
        }

        if (!expiredOrders.isEmpty()) {
            log.info("Expired {} limit orders", expiredOrders.size());
        }
    }

    public List<LimitOrderResponse> getUserLimitOrders(Long userId) {
        List<LimitOrder> orders = limitOrderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::mapToLimitOrderResponse)
                .collect(Collectors.toList());
    }

    public List<LimitOrderResponse> getUserPendingLimitOrders(Long userId) {
        List<LimitOrder> orders = limitOrderRepository.findByUserIdAndStatus(
                userId, LimitOrder.LimitOrderStatus.PENDING);
        return orders.stream()
                .map(this::mapToLimitOrderResponse)
                .collect(Collectors.toList());
    }

    private LimitOrderResponse mapToLimitOrderResponse(LimitOrder order) {
        return LimitOrderResponse.builder()
                .id(order.getId())
                .stockSymbol(order.getStock().getSymbol())
                .companyName(order.getStock().getCompanyName())
                .type(order.getType())
                .limitPrice(order.getLimitPrice())
                .quantity(order.getQuantity())
                .status(String.valueOf(order.getStatus()))
                .createdAt(order.getCreatedAt())
                .expiresAt(order.getExpiresAt())
                .build();
    }
}