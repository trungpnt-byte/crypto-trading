package com.aquarius.crypto.dto.third_party;

import io.netty.util.concurrent.Ticker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Set;

@Component
@Slf4j
public class BinanceTickerProvider implements MarketTickerProvider {

    private static final String SOURCE = "BINANCE";
    @Value("${crypto.sources.binance.url}")
    private static final String apiUrl = "https://api.binance.com/api/v3/ticker/bookTicker";
    private final WebClient tickerWebClient;

    public BinanceTickerProvider(WebClient tickerWebClient) {
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
                .bodyToFlux(BinanceTickerResponse.class)
                .filter(ticker -> supportedPairs.contains(ticker.getSymbol()))
                .map(binanceTicket -> binanceTicket.toTickerResponse(SOURCE))
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(throwable ->
                        {
                            log.error("[BINANCE TICKER PROVIDER] Error fetching tickers from Binance: {}", throwable.getMessage());
                            return Flux.empty();
                        }
                );
    }
}
