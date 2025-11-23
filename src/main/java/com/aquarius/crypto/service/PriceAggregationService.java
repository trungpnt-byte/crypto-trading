package com.aquarius.crypto.service;

import com.aquarius.crypto.dto.response.PriceResponse;
import com.aquarius.crypto.dto.third_party.BinanceTickerResponse;
import com.aquarius.crypto.dto.third_party.HuobiTicker;
import com.aquarius.crypto.dto.third_party.HuobiTickersResponse;
import com.aquarius.crypto.model.PriceAggregation;
import com.aquarius.crypto.repository.PriceAggregationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class PriceAggregationService {

    private static final List<String> TRADING_PAIRS = Arrays.asList("ETHUSDT", "BTCUSDT");
    private static final String BINANCE_API = "https://api.binance.com/api/v3/ticker/bookTicker";
    private static final String HUOBI_API = "https://api.huobi.pro/market/tickers";
    private final PriceAggregationRepository priceRepository;
    private final WebClient.Builder webClientBuilder;

    @Autowired
    public PriceAggregationService(PriceAggregationRepository priceRepository, WebClient.Builder webClientBuilder) {
        this.priceRepository = priceRepository;
        this.webClientBuilder = webClientBuilder;
    }


    @Scheduled(fixedDelayString = "${crypto.scheduler.price-aggregation-interval}")
    public void aggregatePrices() {
        log.info("Starting price aggregation...");

        TRADING_PAIRS.forEach(pair -> Mono.zip(fetchBinancePrice(pair), fetchHuobiPrice(pair)).flatMap(tuple -> {
                    BinanceTickerResponse binance = tuple.getT1();
                    HuobiTicker huobi = tuple.getT2();

                    BigDecimal bestBid = binance.getBidPrice().max(huobi.getBid());
                    BigDecimal bestAsk = binance.getAskPrice().min(huobi.getAsk());

                    PriceAggregation aggregation = PriceAggregation.builder()
                            .tradingPair(pair)
                            .bestBidPrice(bestBid)
                            .bestAskPrice(bestAsk)
                            .source("BINANCE_HUOBI")
                            .createdAt(Instant.now())
                            .build();

                    return priceRepository.save(aggregation);
                })
                .doOnSuccess(saved -> log.info("Saved price for {}: Bid={}, Ask={}",
                        pair, saved.getBestBidPrice(), saved.getBestAskPrice()))
                .doOnError(error -> log.error("Error aggregating price for {}: {}", pair, error.getMessage()))
                .subscribe());
    }

    private Mono<BinanceTickerResponse> fetchBinancePrice(String symbol) {
        return webClientBuilder.build()
                .get()
                .uri(BINANCE_API + "?symbol=" + symbol)
                .retrieve()
                .bodyToMono(BinanceTickerResponse.class)
                .doOnError(error -> log.error("Error fetching Binance price for {}: {}", symbol, error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }

    private Mono<HuobiTicker> fetchHuobiPrice(String symbol) {
        String huobiSymbol = symbol.toLowerCase().replace("usdt", "");

        return webClientBuilder.build()
                .get()
                .uri(HUOBI_API)
                .retrieve()
                .bodyToMono(HuobiTickersResponse.class)
                .flatMapMany(response -> Flux.fromIterable(response.getData()))
                .filter(ticker -> ticker.getSymbol().equalsIgnoreCase(huobiSymbol + "usdt"))
                .next()
                .doOnError(error -> log.error("Error fetching Huobi price for {}: {}", symbol, error.getMessage()))
                .onErrorResume(error -> Mono.empty());
    }

    public Mono<PriceResponse> getLatestPrice(String tradingPair) {
        return priceRepository.findLatestByTradingPair(tradingPair)
                .map(price -> PriceResponse.builder()
                        .tradingPair(price.getTradingPair())
                        .bidPrice(price.getBestBidPrice())
                        .askPrice(price.getBestAskPrice())
                        .source(price.getSource())
                        .timestamp(price.getCreatedAt())
                        .build());
    }
}
