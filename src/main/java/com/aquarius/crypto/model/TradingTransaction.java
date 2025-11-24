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
@Table(name = "trading_transactions")
public class TradingTransaction {
    @Id
    private Long id;
    private Long userId;
    private String symbol;
    private TradeType tradeType;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private String status;
    private Instant createdAt;
}