package com.aquarius.crypto.service;

import com.aquarius.crypto.dto.response.AggregatedPriceResponse;
import com.aquarius.crypto.dto.third_party.MarketTickerProvider;
import com.aquarius.crypto.dto.third_party.TickerResponse;
import com.aquarius.crypto.model.PriceAggregation;
import com.aquarius.crypto.repository.PriceAggregationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PriceAggregationService {
    private static final String SVC_NAME = "[PriceAggregationService] ";
    private final List<MarketTickerProvider> marketTickerProviders;
    private final PriceAggregationRepository priceAggregationRepository;
    @Value("${crypto.supported-pairs:ETHUSDT,BTCUSDT}")
    private Set<String> SUPPORTED_PAIRS = new HashSet<>(Arrays.asList("ETHUSDT", "BTCUSDT"));

    public PriceAggregationService(List<MarketTickerProvider> marketTickerProviders, PriceAggregationRepository priceAggregationRepository) {
        this.marketTickerProviders = marketTickerProviders;
        this.priceAggregationRepository = priceAggregationRepository;
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

    public Mono<BigDecimal> bestPrice(String symbol, String tradeType) {
        return priceAggregationRepository.findLatestByTradingPair(symbol)
                .switchIfEmpty(Mono.error(new RuntimeException("Price not found")))
                .map(p -> tradeType.equals("BUY") ? p.getBestAskPrice() : p.getBestBidPrice());
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
