package com.aquarius.crypto.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceResponse {
    private String tradingPair;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private String source;
    private Instant timestamp;
}
