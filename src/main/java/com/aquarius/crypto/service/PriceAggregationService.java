package com.aquarius.crypto.service;

import com.aquarius.crypto.dto.response.AggregatedPriceResponse;
import com.aquarius.crypto.dto.third_party.MarketTickerProvider;
import com.aquarius.crypto.dto.third_party.TickerResponse;
import com.aquarius.crypto.exception.StalePriceException;
import com.aquarius.crypto.model.PriceAggregation;
import com.aquarius.crypto.repository.PriceAggregationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import static com.aquarius.crypto.constants.ConstStrings.BTC_PAIR;
import static com.aquarius.crypto.constants.ConstStrings.ETH_PAIR;

@Slf4j
@Service
public class PriceAggregationService {
    private static final String SVC_NAME = "[PriceAggregationService] ";
    @Value("${crypto.supported-pairs:ETHUSDT,BTCUSDT}")
    private static final Set<String> SUPPORTED_PAIRS = new HashSet<>(Arrays.asList(ETH_PAIR, BTC_PAIR));
    private final List<MarketTickerProvider> marketTickerProviders;
    private final PriceAggregationRepository priceAggregationRepository;
    private final SecurityContextService securityContextService;
    // Maximum age for a trade price.
    // Scheduler runs every 10s, so 15s gives a 5s buffer.
    @Value("${crypto.trading.stale-price-threshold-seconds:15}")
    private long stalePriceThresholdSeconds;

    public PriceAggregationService(List<MarketTickerProvider> marketTickerProviders, PriceAggregationRepository priceAggregationRepository, SecurityContextService securityContextService) {
        this.marketTickerProviders = marketTickerProviders;
        this.priceAggregationRepository = priceAggregationRepository;
        this.securityContextService = securityContextService;
    }

    public Mono<BigDecimal> bestPrice(String symbol, String tradeType) {
        return priceAggregationRepository.findLatestByTradingPair(symbol)
                .switchIfEmpty(Mono.error(new RuntimeException("Price not found")))
                .flatMap(this::checkPriceFreshness)
                .map(p -> tradeType.equals("BUY") ? p.getBestAskPrice() : p.getBestBidPrice());
    }

    public Mono<AggregatedPriceResponse> findLatestByTradingPair(String symbol) {
        return priceAggregationRepository.findLatestByTradingPair(symbol)
                .map(lp -> new AggregatedPriceResponse(
                        lp.getTradingPair(),
                        lp.getBestBidPrice(),
                        lp.getBestAskPrice(),
                        lp.getCreatedAt()
                ));
    }

    @Scheduled(fixedDelayString = "${crypto.scheduler.price-aggregation-interval}")
    public void aggregatePrices() {
        log.info(SVC_NAME + "Starting price aggregation...");
        Flux.fromIterable(marketTickerProviders)
                .flatMap(provider -> provider.fetchTickers(SUPPORTED_PAIRS))
                .collectMultimap(TickerResponse::getSymbol)
                .flatMap(this::processGroupedTickers)
                .subscribe();
    }

    private Mono<Void> processGroupedTickers(Map<String, Collection<TickerResponse>> groupedData) {
        List<PriceAggregation> aggregatedPricesToSave = new ArrayList<>();

        for (String pair : SUPPORTED_PAIRS) {
            Collection<TickerResponse> tickers = groupedData.get(pair);

            if (tickers == null || tickers.isEmpty()) {
                log.warn("No price data received for {}", pair);
                continue;
            }

            TickerResponse bestBidTicker = null;
            TickerResponse bestAskTicker = null;
            for (TickerResponse t : tickers) {
                if (t.getBidPrice() != null &&
                        (bestBidTicker == null || t.getBidPrice().compareTo(bestBidTicker.getBidPrice()) > 0)) {
                    bestBidTicker = t;
                }
                if (t.getAskPrice() != null &&
                        (bestAskTicker == null || t.getAskPrice().compareTo(bestAskTicker.getAskPrice()) < 0)) {
                    bestAskTicker = t;
                }
            }

            if (bestBidTicker == null || bestAskTicker == null) {
                continue;
            }

            aggregatedPricesToSave.add(PriceAggregation.builder()
                    .tradingPair(pair)
                    .bestBidPrice(bestBidTicker.getBidPrice())
                    .bestAskPrice(bestAskTicker.getAskPrice())
                    .source(formatSources(tickers, bestBidTicker.getBidPrice(), bestAskTicker.getAskPrice()))
                    .createdAt(Instant.now())
                    .build());
        }

        if (aggregatedPricesToSave.isEmpty()) {
            return Mono.empty();
        }

        return priceAggregationRepository.saveAll(aggregatedPricesToSave)
                .doOnComplete(() -> log.info(SVC_NAME + "Saved {} aggregated prices", aggregatedPricesToSave.size()))
                .then();
    }

    /**
     * Checks if the price entity is too old. If so, throws an exception.
     */
    private Mono<PriceAggregation> checkPriceFreshness(PriceAggregation price) {
        Duration age = Duration.between(price.getCreatedAt(), Instant.now());

        if (age.getSeconds() > stalePriceThresholdSeconds) {
            return Mono.error(new StalePriceException(
                    "Trade rejected: Price for " + price.getTradingPair() + " is stale (" + age.getSeconds() + "s old)."
            ));
        }
        return Mono.just(price);
    }

    private String formatSources(Collection<TickerResponse> tickers, BigDecimal bestBid, BigDecimal bestAsk) {
        return tickers.stream()
                .map(t -> {
                    String s = "";
                    if (t.getBidPrice().compareTo(bestBid) == 0) s += t.getSource() + "_BID";
                    if (t.getAskPrice().compareTo(bestAsk) == 0) s += (s.isEmpty() ? "" : "|") + t.getSource() + "_ASK";
                    return s;
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining("|"));
    }


}
