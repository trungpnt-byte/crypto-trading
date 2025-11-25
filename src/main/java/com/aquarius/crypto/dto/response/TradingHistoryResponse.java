package com.aquarius.crypto.dto.response;

import com.aquarius.crypto.model.TradingTransaction;
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
public class TradingHistoryResponse {
    private Long transactionId;
    private String symbol;
    private String tradeType;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private String status;
    private Instant timestamp;

    public static TradingHistoryResponse fromEntity(TradingTransaction history) {
        return TradingHistoryResponse.builder()
                .transactionId(history.getId())
                .symbol(history.getSymbol())
                .tradeType(history.getTradeType().getCode())
                .quantity(history.getQuantity())
                .price(history.getPrice())
                .totalAmount(history.getTotalAmount())
                .status(history.getStatus())
                .timestamp(history.getCreatedAt())
                .build();
    }
}
