package com.aquarius.crypto.controller;

import com.aquarius.crypto.config.security.SecurityConfig;
import com.aquarius.crypto.dto.TradeType;
import com.aquarius.crypto.service.SecurityContextService;
import com.aquarius.crypto.service.TradingService;
import com.aquarius.crypto.service.UserMappingService;
import com.aquarius.crypto.service.WalletService;
import com.aquarius.crypto.common.LocalApiResponse;
import com.aquarius.crypto.dto.request.TradingRequest;
import com.aquarius.crypto.model.TradingTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Collections;

import static com.aquarius.crypto.constants.ConstStrings.ETH_PAIR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@Disabled("TODO: Fix integration test setup")
@Import({SecurityConfig.class})
class TradingControllerIntegrationTest {

    private static final Long TEST_INTERNAL_ID = 100L;
    @Mock
    private WebTestClient webTestClient;
    @Mock
    private TradingService tradingService;
    @Mock
    private WalletService walletService;

    @Mock
    private UserMappingService userMappingService;

    private final SecurityContextService securityContextService = new SecurityContextService(userMappingService);
    private TradingRequest validBuyRequest;

    @BeforeEach
    void setUp() {
        when(securityContextService.getInternalUserId()).thenReturn(Mono.just(TEST_INTERNAL_ID));

        validBuyRequest = TradingRequest.builder()
                .symbol(ETH_PAIR)
                .tradeType("BUY")
                .quantity(new BigDecimal("0.5"))
                .userId(999L)
                .build();
    }

    @Test
//    @WithMockUser(roles = "TRADER", username = "testuser_uuid")
    void executeTrade_ShouldReturn201Created_AndOverwriteUserId() {
        TradingTransaction mockTx = TradingTransaction.builder()
                .userId(TEST_INTERNAL_ID)
                .tradeType(TradeType.BUY)
                .symbol(ETH_PAIR)
                .status("COMPLETED")
                .build();
        when(tradingService.trade(any(TradingRequest.class))).thenReturn(Mono.just(mockTx));

        webTestClient.post().uri("/api/v1/trades")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validBuyRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(LocalApiResponse.class)
                .consumeWith(response -> {
                    verify(tradingService).trade(argThat(req -> req.getUserId().equals(TEST_INTERNAL_ID)));
                });
    }

    @Test
//    @WithMockUser(roles = "TRADER")
    void getMyWalletBalances_ShouldReturn200OK() {
        when(walletService.getUserWalletBalances()).thenReturn(Flux.fromIterable(Collections.emptyList()));

        webTestClient.get().uri("/api/v1/wallets/me/all")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200);

        verify(walletService).getUserWalletBalances();
    }

    @Test
    void executeTrade_WithoutToken_ShouldReturn401Unauthorized() {
        webTestClient.post().uri("/api/v1/trades")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validBuyRequest)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
