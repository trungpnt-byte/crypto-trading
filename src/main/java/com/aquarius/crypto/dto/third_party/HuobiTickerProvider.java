package com.aquarius.crypto.dto.third_party;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Set;

@Slf4j
@Component
public class HuobiTickerProvider implements MarketTickerProvider {
    private static final String SOURCE = "HUOBI";
    @Value("${crypto.sources.huobi.url}")
    private static final String apiUrl = "https://api.huobi.pro/market/tickers";

    private final WebClient tickerWebClient;

    public HuobiTickerProvider(WebClient tickerWebClient) {
        this.tickerWebClient = tickerWebClient;
    }

    @Override
    public String getMarketSource() {
        return SOURCE;
    }

    @Override
    public Flux<TickerResponse> fetchTickers(Set<String> supportedPairs) {
        return tickerWebClient.get().uri(apiUrl)
                .retrieve()
                .bodyToMono(HuobiTickersWrapper.class)
                .flatMapIterable(HuobiTickersWrapper::getData)
                .map(t -> t.toTickerResponse(getMarketSource()))
                .filter(t -> supportedPairs.contains(t.getSymbol().toUpperCase()))
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(e -> {
                    log.warn("Huobi fetch failed: {}", e.getMessage());
                    return Flux.empty();
                });
    }
}
