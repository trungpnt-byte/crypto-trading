package com.aquarius.crypto.service.trading_transaction;

import com.aquarius.crypto.dto.TradeType;
import com.aquarius.crypto.dto.request.TradingRequest;
import com.aquarius.crypto.model.PriceAggregation;
import com.aquarius.crypto.model.TradingTransaction;
import com.aquarius.crypto.model.Wallet;
import com.aquarius.crypto.repository.PriceAggregationRepository;
import com.aquarius.crypto.repository.TradingTransactionRepository;
import com.aquarius.crypto.repository.WalletRepository;
import com.aquarius.crypto.service.PriceAggregationService;
import com.aquarius.crypto.service.TradingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradingServiceTest {

    @Mock
    private TradingTransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PriceAggregationRepository priceRepository;

    @Mock
    private PriceAggregationService priceAggregationService;

    @InjectMocks
    private TradingService tradingService;

    private Long userId;
    private TradingRequest buyRequest;
    private PriceAggregation priceAggregation;
    private Wallet usdtWallet;
    private Wallet ethWallet;

    @BeforeEach
    void setUp() {
        userId = 1L;

        buyRequest = TradingRequest.builder()
                .tradingPair("ETHUSDT")
                .tradeType("BUY")
                .symbol("ETHUSDT")
                .quantity(new BigDecimal("1.0"))
                .build();

        priceAggregation = PriceAggregation.builder()
                .id(1L)
                .tradingPair("ETHUSDT")
                .bestBidPrice(new BigDecimal("2000.00"))
                .bestAskPrice(new BigDecimal("2001.00"))
                .source("BINANCE_HUOBI")
                .createdAt(Instant.now())
                .build();

        usdtWallet = Wallet.builder()
                .id(1L)
                .userId(userId)
                .currency("USDT")
                .balance(new BigDecimal("50000.00"))
                .build();

        ethWallet = Wallet.builder()
                .id(2L)
                .userId(userId)
                .currency("ETH")
                .balance(BigDecimal.ZERO)
                .build();
    }

    @Test
    void executeBuyTrade_Success() {
        when(priceRepository.findLatestByTradingPair("ETHUSDT"))
                .thenReturn(Mono.just(priceAggregation));
        when(walletRepository.findByUserIdAndCurrency(userId, "USDT"))
                .thenReturn(Mono.just(usdtWallet));
        when(walletRepository.findByUserIdAndCurrency(userId, "ETH"))
                .thenReturn(Mono.just(ethWallet));
        when(walletRepository.save(any(Wallet.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        TradingTransaction savedTransaction = TradingTransaction.builder()
                .id(1L)
                .userId(userId)
                .tradingPair("ETHUSDT")
                .tradeType(TradeType.valueOf("BUY"))
                .quantity(new BigDecimal("1.0"))
                .price(new BigDecimal("2001.00"))
                .totalAmount(new BigDecimal("2001.00"))
                .status("COMPLETED")
                .createdAt(Instant.now())
                .build();

        when(transactionRepository.save(any(TradingTransaction.class)))
                .thenReturn(Mono.just(savedTransaction));

        StepVerifier.create(tradingService.executeTrade(buyRequest))
                .expectNextMatches(response ->
                        response.getId().equals(1L) &&
                                response.getTradeType().equals("BUY") &&
                                response.getStatus().equals("COMPLETED")
                ).verifyComplete();

        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(1)).save(any(TradingTransaction.class));
    }
}
