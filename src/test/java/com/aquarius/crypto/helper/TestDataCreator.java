package com.aquarius.crypto.helper;

import com.aquarius.crypto.dto.third_party.TickerResponse;
import com.aquarius.crypto.model.Wallet;

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

    public static Wallet createWallet(Long id, String currency, String balance) {
        return Wallet.builder()
                .id(id)
                .userId(1L)
                .currency(currency)
                .balance(new BigDecimal(balance))
                .version(1L)
                .build();
    }
}
