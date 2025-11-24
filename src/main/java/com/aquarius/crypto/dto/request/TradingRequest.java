package com.aquarius.crypto.dto.request;

import lombok.*;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradingRequest {
    private Long userId;
    private String symbol; // e.g., "ETHUSDT"
    private String tradeType; // "BUY" or "SELL"
    private BigDecimal quantity; // Amount of base currency (ETH/BTC) to trade

    public void validate() {
        if (symbol == null || tradeType == null || quantity == null) {
            throw new IllegalArgumentException("All fields must be provided");
        }
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (!tradeType.equals("BUY") && !tradeType.equals("SELL")) {
            throw new IllegalArgumentException("Trade type must be either BUY or SELL");
        }
    }
}
