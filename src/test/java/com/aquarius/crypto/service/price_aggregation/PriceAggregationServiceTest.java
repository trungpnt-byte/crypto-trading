package com.aquarius.crypto.service.price_aggregation;

import com.aquarius.crypto.dto.third_party.MarketTickerProvider;
import com.aquarius.crypto.model.PriceAggregation;
import com.aquarius.crypto.repository.PriceAggregationRepository;
import com.aquarius.crypto.service.PriceAggregationService;
import com.aquarius.crypto.service.SecurityContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.util.List;

import static com.aquarius.crypto.constants.ConstStrings.BTC_PAIR;
import static com.aquarius.crypto.constants.ConstStrings.ETH_PAIR;
import static com.aquarius.crypto.helper.TestDataCreator.createTicker;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PriceAggregationServiceTest {

    @Mock
    private MarketTickerProvider binanceProvider;
    @Mock
    private MarketTickerProvider huobiProvider;
    @Mock
    private PriceAggregationRepository repository;
    @Mock
    private SecurityContextService securityContextService;

    @Captor
    private ArgumentCaptor<List<PriceAggregation>> pricesCaptor;

    private PriceAggregationService service;

    @BeforeEach
    void setUp() {
        service = new PriceAggregationService(
                List.of(binanceProvider, huobiProvider),
                repository, securityContextService
        );
    }

    @Test
    void testAggregatePrices_SuccessfulAggregation() {
        // GIVEN: Binance has better Bid (Buy), Huobi has better Ask (Sell)
        when(binanceProvider.fetchTickers(any())).thenReturn(Flux.just(
                createTicker("BINANCE", ETH_PAIR, "2000.00", "2010.00") // High Bid
        ));
        when(huobiProvider.fetchTickers(any())).thenReturn(Flux.just(
                createTicker("HUOBI", ETH_PAIR, "1990.00", "2005.00")   // Low Ask
        ));

        // WHEN
        service.aggregatePrices();

        verify(repository).saveAll(pricesCaptor.capture());
        List<PriceAggregation> savedPrices = pricesCaptor.getValue();

        // THEN
        assertEquals(1, savedPrices.size());
        PriceAggregation ethPrice = savedPrices.get(0);

        assertEquals(ETH_PAIR, ethPrice.getTradingPair());
        assertEquals(new BigDecimal("2000.00"), ethPrice.getBestBidPrice(), "Should take Binance Bid");
        assertEquals(new BigDecimal("2005.00"), ethPrice.getBestAskPrice(), "Should take Huobi Ask");

        // Check source tagging logic
        assertTrue(ethPrice.getSource().contains("BINANCE_BID"));
        assertTrue(ethPrice.getSource().contains("HUOBI_ASK"));
    }

    @Test
    void testAggregatePrices_Resilience_OneSourceFails() {
        // GIVEN: Binance fails, Huobi works
        // Note: In the Service, we handle exceptions inside the Provider,
        // so the Provider returns Flux.empty() on failure.
        when(binanceProvider.fetchTickers(any())).thenReturn(Flux.empty());

        when(huobiProvider.fetchTickers(any())).thenReturn(Flux.just(
                createTicker("HUOBI", ETH_PAIR, "1995.00", "2005.00")
        ));

        // WHEN
        service.aggregatePrices();

        // THEN: We should STILL save Huobi prices (No "continue if < 2" logic)
        verify(repository).saveAll(pricesCaptor.capture());
        List<PriceAggregation> savedPrices = pricesCaptor.getValue();

        assertEquals(1, savedPrices.size());
        PriceAggregation ethPrice = savedPrices.get(0);

        assertEquals(new BigDecimal("1995.00"), ethPrice.getBestBidPrice());
        assertTrue(ethPrice.getSource().contains("HUOBI"));
    }

    @Test
    void testAggregatePrices_TieBreaker() {
        // GIVEN: Both have exact same prices
        when(binanceProvider.fetchTickers(any())).thenReturn(Flux.just(
                createTicker("BINANCE", BTC_PAIR, "50000.00", "50100.00")
        ));
        when(huobiProvider.fetchTickers(any())).thenReturn(Flux.just(
                createTicker("HUOBI", BTC_PAIR, "50000.00", "50100.00")
        ));

        // WHEN
        service.aggregatePrices();

        // THEN
        verify(repository).saveAll(pricesCaptor.capture());
        PriceAggregation btcPrice = pricesCaptor.getValue().get(0);

        String source = btcPrice.getSource();
        // "BINANCE_BID|HUOBI_BID|BINANCE_ASK|HUOBI_ASK" (order may vary)
        assertTrue(source.contains("BINANCE_BID"));
        assertTrue(source.contains("HUOBI_BID"));
        assertTrue(source.contains("BINANCE_ASK"));
    }

    @Test
    void testAggregatePrices_NoData_DoesNotSave() {
        // GIVEN: Both return empty
        when(binanceProvider.fetchTickers(any())).thenReturn(Flux.empty());
        when(huobiProvider.fetchTickers(any())).thenReturn(Flux.empty());

        // WHEN
        service.aggregatePrices();

        // THEN
        verify(repository, never()).saveAll((Iterable<PriceAggregation>) any());
    }
}