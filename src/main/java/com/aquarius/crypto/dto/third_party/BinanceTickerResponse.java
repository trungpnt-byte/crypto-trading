package com.aquarius.crypto.dto.third_party;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BinanceTickerResponse {
    private String symbol;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;

    public TickerResponse toTickerResponse(String source) {
        return new TickerResponse(
                this.symbol,
                this.bidPrice,
                this.askPrice,
                source
        );
    }
}