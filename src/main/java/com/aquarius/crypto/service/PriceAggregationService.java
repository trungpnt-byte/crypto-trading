package com.aquarius.crypto.service;

import com.aquarius.crypto.dto.third_party.HuobiTickersWrapper;
import com.aquarius.crypto.dto.response.AggregatedPriceResponse;
import com.aquarius.crypto.dto.third_party.BinanceTickerResponse;
import com.aquarius.crypto.dto.third_party.TickerResponse;
import com.aquarius.crypto.model.PriceAggregation;
import com.aquarius.crypto.repository.PriceAggregationRepository;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import java.util.Collections;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class PriceAggregationService {

    private static final Set<String> SUPPORTED_PAIRS = new HashSet<>() {{
        add("ETHUSDT");
        add("BTCUSDT");
    }};

    private static final String BINANCE_API = "https://api.binance.com/api/v3/ticker/bookTicker";
    private static final String HUOBI_API = "https://api.huobi.pro/market/tickers";

    private final PriceAggregationRepository priceAggregationRepository;
    private final WebClient tickerWebClient;

    public PriceAggregationService(PriceAggregationRepository priceAggregationRepository, WebClient tickerWebClient) {
        this.priceAggregationRepository = priceAggregationRepository;
        this.tickerWebClient = tickerWebClient;
    }

    public Mono<BigDecimal> bestPrice(String symbol, String tradeType) {
        return priceAggregationRepository.findLatestByTradingPair(symbol)
                .switchIfEmpty(Mono.error(new RuntimeException("Price not found")))
                .map(p -> tradeType.equals("BUY") ? p.getBestAskPrice() : p.getBestBidPrice());
    }

    @Scheduled(fixedDelayString = "${crypto.scheduler.price-aggregation-interval}")
    public void aggregatePrices() {
        log.info("Starting price aggregation...");
        Map<String, Collection<TickerResponse>> groupedTickers = Flux.merge(fetchBinancePrices(), fetchHuobiPrices()).collectMultimap(TickerResponse::getSymbol).block();

        List<PriceAggregation> latestPrices = aggregateAndPrepareSaves(groupedTickers).block();
        if (latestPrices == null || latestPrices.isEmpty()) {
            log.warn("No latest prices to save after aggregation.");
            return;
        }
        priceAggregationRepository.saveAll(latestPrices)
                .doOnComplete(() -> log.info("Price aggregation completed successfully."))
                .doOnError(e -> log.error("Overall aggregation failed: {}", e.getMessage()))
                .subscribe();
    }

    /**
     * Calls Binance API once to get ALL tickers, then filters for supported pairs.
     */
    private Flux<TickerResponse> fetchBinancePrices() {
        return tickerWebClient.get().uri(BINANCE_API)
                .retrieve()
                .bodyToFlux(BinanceTickerResponse.class)
                .filter(t -> SUPPORTED_PAIRS.contains(t.getSymbol()))
                .map(t -> t.toTickerResponse("BINANCE"))
                .doOnError(e -> log.error("Error fetching Binance prices: {}", e.getMessage()))
                .onErrorResume(e -> {
                    log.error("Binance fetch failed: {}", e.getMessage());
                    return Flux.empty(); // Fail silently for this source, continue with Huobi
                });
    }

    /**
     * Calls Huobi API once to get ALL tickers, then extracts and filters.
     */
    private Flux<TickerResponse> fetchHuobiPrices() {
        return tickerWebClient.get().uri(HUOBI_API)
                .retrieve()
                .bodyToMono(HuobiTickersWrapper.class)
                .flatMapIterable(HuobiTickersWrapper::getData) // Extract the List<HuobiTicker>
                .map(t -> t.toTickerResponse("HUOBI"))
                .filter(t -> SUPPORTED_PAIRS.contains(t.getSymbol().toUpperCase()))
                .doOnError(e -> log.error("Error fetching Huobi prices: {}", e.getMessage()))
                .onErrorResume(e -> {
                    log.error("Huobi fetch failed: {}", e.getMessage());
                    return Flux.empty(); // Fail silently for this source, continue with Binance
                });
    }

    private Mono<List<PriceAggregation>> aggregateAndPrepareSaves(Map<String, Collection<TickerResponse>> groupedTickers) {
        if (groupedTickers == null || groupedTickers.isEmpty()) {
            log.error("No ticker data available for aggregation.");
            return Mono.just(Collections.emptyList());
        }
        List<PriceAggregation> latestPrices = new ArrayList<>();

        for (String pair : SUPPORTED_PAIRS) {
            Collection<TickerResponse> tickers = groupedTickers.getOrDefault(pair, Collections.emptyList());

            if (tickers.size() < 2) { // Expect at least two sources
                log.warn("Only {} sources provided data for {}. Skipping aggregation.", tickers.size(), pair);
                continue;
            }

            BigDecimal bestBid = null;
            BigDecimal bestAsk = null;

            for (TickerResponse t : tickers) {
                if (t.getBidPrice() != null && (bestBid == null || t.getBidPrice().compareTo(bestBid) > 0)) {
                    bestBid = t.getBidPrice();
                }
                if (t.getAskPrice() != null && (bestAsk == null || t.getAskPrice().compareTo(bestAsk) < 0)) {
                    bestAsk = t.getAskPrice();
                }
            }

            if (bestBid == null || bestAsk == null) {
                log.warn("Incomplete price data for {}. Bid/Ask is missing.", pair);
                continue;
            }

            PriceAggregation latestPrice = PriceAggregation.builder()
                    .bestBidPrice(bestBid)
                    .bestAskPrice(bestAsk)
                    .tradingPair(pair)
                    .createdAt(Instant.now())
                    .source(determineSource(tickers, bestBid, bestAsk))
                    .build();

            latestPrices.add(latestPrice);
        }

        return Mono.just(latestPrices);
    }

    /**
     * Determines which exchange(s) provided the best Bid and Ask prices.
     */
    private String determineSource(Collection<TickerResponse> tickers, BigDecimal bestBid, BigDecimal bestAsk) {
        Set<String> bestSources = new HashSet<>();
        for (TickerResponse ticker : tickers) {
            if (ticker.getBidPrice().compareTo(bestBid) == 0) {
                bestSources.add(ticker.getSource() + "_BID");
            }
            if (ticker.getAskPrice().compareTo(bestAsk) == 0) {
                bestSources.add(ticker.getSource() + "_ASK");
            }
        }
        return String.join(" | ", bestSources);
    }

    //TODO for unit tests
    public Publisher<PriceAggregation> aggregateAndSavePrices() {
        return Flux.fromIterable(Collections.singleton(new PriceAggregation()));
    }

    public Mono<AggregatedPriceResponse> findByTradingPair(String symbol) {
        return priceAggregationRepository.findLatestByTradingPair(symbol)
                .map(lp -> new AggregatedPriceResponse(
                        lp.getTradingPair(),
                        lp.getBestBidPrice(),
                        lp.getBestAskPrice(),
                        lp.getCreatedAt()
                ));
    }

}
