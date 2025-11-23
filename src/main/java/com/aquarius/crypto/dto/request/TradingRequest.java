package com.aquarius.crypto.dto.request;

import com.aquarius.crypto.dto.TradeType;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TradingRequest {
    private Long userId;
    private String symbol; // e.g., "ETHUSDT"
    private String tradingPair;
    private String tradeType; // "BUY" or "SELL"
    private BigDecimal quantity; // Amount of base currency (ETH/BTC) to trade
}
