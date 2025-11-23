package com.aquarius.crypto.dto.third_party;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TickerResponse {
    private String symbol;      // e.g., ETHUSDT
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private String source;      // BINANCE or HUOBI
}
