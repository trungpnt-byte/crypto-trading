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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
        log.info(SVC_NAME + " Starting price aggregation...");

        Flux.fromIterable(marketTickerProviders)
                .flatMap(provider -> provider.fetchTickers(SUPPORTED_PAIRS))
                .collectList()
                .flatMap(this::processTickers)
                .subscribe();
    }


    private Mono<Void> processTickers(List<TickerResponse> allTickers) {
        Map<String, TickerResponse[]> bestMap = new HashMap<>();
        Map<String, StringBuilder> sourcesMap = new HashMap<>();

        for (TickerResponse t : allTickers) {
            String symbol = t.getSymbol();
            TickerResponse[] pair = bestMap.getOrDefault(symbol, new TickerResponse[2]);
            StringBuilder sources = sourcesMap.getOrDefault(symbol, new StringBuilder());

            // Update best bid
            if (t.getBidPrice() != null &&
                    (pair[0] == null || t.getBidPrice().compareTo(pair[0].getBidPrice()) > 0)) {
                pair[0] = t;
                sources.setLength(0); // reset sources
                sources.append(t.getSource()).append("_BID");
            } else if (pair[0] != null && t.getBidPrice() != null &&
                    t.getBidPrice().compareTo(pair[0].getBidPrice()) == 0) {
                if (!sources.isEmpty()) {
                    sources.append("|");
                }
                sources.append(t.getSource()).append("_BID");
            }

            // Update best ask
            if (t.getAskPrice() != null &&
                    (pair[1] == null || t.getAskPrice().compareTo(pair[1].getAskPrice()) < 0)) {
                pair[1] = t;
                if (!sources.isEmpty()) sources.append("|");
                sources.append(t.getSource()).append("_ASK");
            } else if (pair[1] != null && t.getAskPrice() != null &&
                    t.getAskPrice().compareTo(pair[1].getAskPrice()) == 0) {
                if (!sources.isEmpty() && !sources.toString().contains(t.getSource() + "_ASK")) {
                    sources.append("|").append(t.getSource()).append("_ASK");
                }
            }

            bestMap.put(symbol, pair);
            sourcesMap.put(symbol, sources);
        }

        List<PriceAggregation> aggregatedPricesToSave = bestMap.entrySet().stream()
                .map(e -> {
                    TickerResponse[] pair = e.getValue();
                    if (pair[0] == null || pair[1] == null) return null;

                    return PriceAggregation.builder()
                            .tradingPair(e.getKey())
                            .bestBidPrice(pair[0].getBidPrice())
                            .bestAskPrice(pair[1].getAskPrice())
                            .source(sourcesMap.get(e.getKey()).toString())
                            .createdAt(Instant.now())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();

        if (aggregatedPricesToSave.isEmpty()) {
            return Mono.empty();
        }

        return priceAggregationRepository.saveAll(aggregatedPricesToSave)
                .doOnComplete(() -> log.info(SVC_NAME + " Saved {} aggregated prices", aggregatedPricesToSave.size()))
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
}
