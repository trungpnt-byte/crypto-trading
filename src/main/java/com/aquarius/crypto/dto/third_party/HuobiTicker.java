package com.aquarius.crypto.dto.third_party;

import lombok.Data;
import lombok.Getter;

import java.math.BigDecimal;

@Data
@Getter
public class HuobiTicker {
    private String symbol;
    private BigDecimal bid;
    private BigDecimal ask;
}

