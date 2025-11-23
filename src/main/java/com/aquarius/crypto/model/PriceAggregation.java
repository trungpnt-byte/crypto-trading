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
@Table(name = "price_aggregations")
public class PriceAggregation {
    @Id
    private Long id;
    private String tradingPair;
    private BigDecimal bestBidPrice;
    private BigDecimal bestAskPrice;
    private String source;
    private Instant createdAt;
}