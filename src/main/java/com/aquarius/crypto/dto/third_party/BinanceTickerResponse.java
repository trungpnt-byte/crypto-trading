package com.aquarius.crypto.dto.third_party;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Data
@Getter
public class BinanceTickerResponse {
    private String symbol;

    @JsonProperty("bidPrice")
    private BigDecimal bidPrice;

    @JsonProperty("askPrice")
    private BigDecimal askPrice;
}