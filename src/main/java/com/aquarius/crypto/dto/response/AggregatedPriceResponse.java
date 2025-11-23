package com.aquarius.crypto.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class AggregatedPriceResponse {
    private String tradingPair;
    private BigDecimal bestBidPrice;
    private BigDecimal bestAskPrice;
    private String source;
    private Instant timestamp;

    public AggregatedPriceResponse(String tradingPair, BigDecimal bestBidPrice, BigDecimal bestAskPrice, Instant createdAt) {
        this.tradingPair = tradingPair;
        this.bestBidPrice = bestBidPrice;
        this.bestAskPrice = bestAskPrice;
        this.timestamp = createdAt;
    }
}
