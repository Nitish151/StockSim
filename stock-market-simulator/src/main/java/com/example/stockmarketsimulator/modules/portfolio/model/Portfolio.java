package com.example.stockmarketsimulator.modules.portfolio.model;

import com.example.stockmarketsimulator.modules.stock.model.Stock;
import com.example.stockmarketsimulator.modules.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.autoconfigure.batch.BatchTransactionManager;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;

@Entity
@Table(name="portfolios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    private int quantity;

}
