package com.aquarius.crypto.dto.third_party;

import reactor.core.publisher.Flux;

import java.util.Set;

public interface MarketTickerProvider {
    String getMarketSource();

    Flux<TickerResponse> fetchTickers(Set<String> supportedPairs);
}
