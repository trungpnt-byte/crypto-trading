package com.aquarius.crypto.helper;

import com.aquarius.crypto.dto.third_party.TickerResponse;

import java.math.BigDecimal;

public class TestDataCreator {
    public static TickerResponse createTicker(String source, String symbol, String bid, String ask) {
        return TickerResponse.builder()
                .source(source)
                .symbol(symbol)
                .bidPrice(new BigDecimal(bid))
                .askPrice(new BigDecimal(ask))
                .build();
    }
}
