package com.aquarius.crypto.service.price_aggregation;

import com.aquarius.crypto.model.PriceAggregation;
import com.aquarius.crypto.repository.PriceAggregationRepository;
import com.aquarius.crypto.service.PriceAggregationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StatePriceAggregationServiceTest {

    private static final long TEST_STALE_THRESHOLD_SECONDS = 10;
    private static final String SYMBOL = "ETHUSDT";
    private static final BigDecimal ASK_PRICE = new BigDecimal("3000.50");
    private static final BigDecimal BID_PRICE = new BigDecimal("3000.00");
    @Mock
    private PriceAggregationRepository priceAggregationRepository;
    @InjectMocks
    private PriceAggregationService priceAggregationService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(
                priceAggregationService,
                "stalePriceThresholdSeconds",
                TEST_STALE_THRESHOLD_SECONDS
        );
    }

    //  Successful Retrieval (Fresh Price) ---
    @Test
    @DisplayName("Should return the Ask price for BUY order when price is fresh")
    void bestPrice_shouldReturnAskPrice_whenFresh() {
        // Arrange: Price created NOW
        PriceAggregation freshPrice = PriceAggregation.builder()
                .tradingPair(SYMBOL)
                .bestAskPrice(ASK_PRICE)
                .bestBidPrice(BID_PRICE)
                .createdAt(Instant.now())
                .build();

        // Mock repository lookup
        when(priceAggregationRepository.findLatestByTradingPair(anyString()))
                .thenReturn(Mono.just(freshPrice));

        // Act & Assert
        StepVerifier.create(priceAggregationService.bestPrice(SYMBOL, "BUY"))
                .expectNext(ASK_PRICE)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should return the Bid price for SELL order when price is fresh")
    void bestPrice_shouldReturnBidPrice_whenFresh() {
        // Arrange: Price created NOW
        PriceAggregation freshPrice = PriceAggregation.builder()
                .tradingPair(SYMBOL)
                .bestAskPrice(ASK_PRICE)
                .bestBidPrice(BID_PRICE)
                .createdAt(Instant.now())
                .build();

        // Mock repository lookup
        when(priceAggregationRepository.findLatestByTradingPair(anyString()))
                .thenReturn(Mono.just(freshPrice));

        // Act & Assert
        StepVerifier.create(priceAggregationService.bestPrice(SYMBOL, "SELL"))
                .expectNext(BID_PRICE)
                .verifyComplete();
    }

    //  Stale Price Failure ---
    @Test
    @DisplayName("Should throw RuntimeException when price is older than threshold")
    void bestPrice_shouldThrowException_whenStale() {
        // Arrange: Create a stale price (1 second older than the 10s threshold)
        Instant staleTimestamp = Instant.now().minus(TEST_STALE_THRESHOLD_SECONDS + 1, ChronoUnit.SECONDS);

        PriceAggregation stalePrice = PriceAggregation.builder()
                .tradingPair(SYMBOL)
                .bestAskPrice(ASK_PRICE)
                .bestBidPrice(BID_PRICE)
                .createdAt(staleTimestamp)
                .build();

        // Mock repository lookup
        when(priceAggregationRepository.findLatestByTradingPair(anyString()))
                .thenReturn(Mono.just(stalePrice));

        // Act & Assert
        StepVerifier.create(priceAggregationService.bestPrice(SYMBOL, "BUY"))
                .expectErrorSatisfies(e -> {
                    // Assert the exception type and message content
                    assertInstanceOf(RuntimeException.class, e);
                    String message = e.getMessage();
                    assertTrue(message.contains("Trade rejected: Price for ETHUSDT is stale"));
                    assertTrue(message.contains("11s old")); // Since we used 10+1 seconds
                })
                .verify();
    }

    //  Missing Price Failure
    @Test
    @DisplayName("Should throw RuntimeException when price is not found")
    void bestPrice_shouldThrowException_whenNotFound() {
        // Arrange: Mock repository returns empty Mono
        when(priceAggregationRepository.findLatestByTradingPair(anyString()))
                .thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(priceAggregationService.bestPrice(SYMBOL, "BUY"))
                .expectError(RuntimeException.class)
                .verify();
    }
}
