package com.example.stockmarketsimulator.modules.stock.service;

import java.util.Set;

public interface StockTrackingService {
    /**
     * Adds a stock to the user's tracked list
     */
    void trackStock(String username, String symbol);

    /**
     * Removes a stock from the user's tracked list
     */
    void untrackStock(String username, String symbol);

    /**
     * Gets all stocks tracked by a specific user
     */
    Set<String> getUserTrackedStocks(String username);

    /**
     * Gets all stocks tracked by any user
     */
    Set<String> getAllTrackedStocks();

    /**
     * Checks if a stock is not being tracked by any user
     */
    boolean isStockUntracked(String symbol);
}