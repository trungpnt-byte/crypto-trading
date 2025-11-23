package com.aquarius.crypto.model;

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