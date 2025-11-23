package com.aquarius.crypto.model;

import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "trade_transactions")
public class TradingTransaction {
    @Id
    private Long id;
    private Long userId;
    private String tradingPair;
    private String tradeType; // BUY or SELL
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private String status; // COMPLETED, FAILED
    private Instant createdAt;
}