package com.aquarius.crypto.model;

import com.aquarius.crypto.dto.TradeType;
import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "trade_transactions")
public class TradingTransaction {
    @Id
    private Long id;
    private Long userId;
    private String symbol;
    private TradeType tradeType; // BUY or SELL
    private BigDecimal quantity;
    private BigDecimal price; // price per unit of base currency at which the trade was executed
    private BigDecimal totalAmount; // how much crypto was spent/received in the trade
    private String status; // COMPLETED, FAILED
    private Instant createdAt;
}