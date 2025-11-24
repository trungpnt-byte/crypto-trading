package com.aquarius.crypto.dto.third_party;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HuobiTicker {
    private String symbol; // e.g., ethusdt (lowercase)
    private BigDecimal bid;
    private BigDecimal ask;

    public TickerResponse toTickerResponse(String source) {
        String normalizedSymbol = this.symbol.toUpperCase();

        return new TickerResponse(
                normalizedSymbol,
                this.bid,
                this.ask,
                source
        );
    }
}