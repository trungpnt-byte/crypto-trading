package com.aquarius.crypto.service.trading_transaction;

import com.aquarius.crypto.dto.request.TradingRequest;
import com.aquarius.crypto.model.TradingTransaction;
import com.aquarius.crypto.model.Wallet;
import com.aquarius.crypto.repository.TradingTransactionRepository;
import com.aquarius.crypto.service.PriceAggregationService;
import com.aquarius.crypto.service.TradingService;
import com.aquarius.crypto.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static com.aquarius.crypto.helper.TestDataCreator.createWallet;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.springframework.transaction.reactive.TransactionalOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class TradingServiceTest {

    @Mock
    private WalletService walletService;
    @Mock
    private TradingTransactionRepository transactionRepo;
    @Mock
    private PriceAggregationService priceService;
    @Mock
    private TransactionalOperator rxtx;

    private TradingService tradingService;

    @Captor
    private ArgumentCaptor<Wallet> walletCaptor;

    @BeforeEach
    void setUp() {
        tradingService = new TradingService(walletService, transactionRepo, priceService, rxtx);
        lenient().when(rxtx.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testBuyExecution_CorrectlyDebitsQuoteAndCreditsBase() {
        // GIVEN: buy 1.0 ETH at 2000 USDT
        TradingRequest req = new TradingRequest(1L, "ETHUSDT", "BUY", new BigDecimal("1.0"));

        Wallet usdtWallet = createWallet(10L, "USDT", "5000.00");
        Wallet ethWallet = createWallet(20L, "ETH", "0.00");

        when(priceService.bestPrice("ETHUSDT", "BUY")).thenReturn(Mono.just(new BigDecimal("2000.00")));
        when(walletService.findByUserAndCurrency(1L, "USDT")).thenReturn(Mono.just(usdtWallet));
        when(walletService.findByUserAndCurrency(1L, "ETH")).thenReturn(Mono.just(ethWallet));
        when(walletService.save(any(Wallet.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(transactionRepo.save(any())).thenReturn(Mono.just(new TradingTransaction()));

        // WHEN
        StepVerifier.create(tradingService.trade(req))
                .expectNextCount(1)
                .verifyComplete();

        // THEN:
        verify(walletService, times(2)).save(walletCaptor.capture());
        var savedWallets = walletCaptor.getAllValues();

        // Check USDT (Debit)
        Wallet savedUsdt = savedWallets.stream().filter(w -> w.getCurrency().equals("USDT")).findFirst().get();
        assertEquals(0, new BigDecimal("3000.00").compareTo(savedUsdt.getBalance()), "USDT should decrease by Cost (2000)");

        // Check ETH (Credit)
        Wallet savedEth = savedWallets.stream().filter(w -> w.getCurrency().equals("ETH")).findFirst().get();
        assertEquals(0, new BigDecimal("1.0").compareTo(savedEth.getBalance()), "ETH should increase by Quantity (1.0)");
    }

    @Test
    void testSellExecution_CorrectlyDebitsBaseAndCreditsQuote() {
        // GIVEN: User wants to SELL 0.5 ETH at 3000 USDT
        // Cost Logic: 0.5 ETH * 3000 = 1500 USDT Value
        TradingRequest req = new TradingRequest(1L, "ETHUSDT", "SELL", new BigDecimal("0.5"));

        Wallet usdtWallet = createWallet(10L, "USDT", "1000.00");
        Wallet ethWallet = createWallet(20L, "ETH", "2.00");

        when(priceService.bestPrice("ETHUSDT", "SELL")).thenReturn(Mono.just(new BigDecimal("3000.00")));
        when(walletService.findByUserAndCurrency(1L, "USDT")).thenReturn(Mono.just(usdtWallet));
        when(walletService.findByUserAndCurrency(1L, "ETH")).thenReturn(Mono.just(ethWallet));
        when(walletService.save(any(Wallet.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(transactionRepo.save(any())).thenReturn(Mono.just(new TradingTransaction()));

        // WHEN
        StepVerifier.create(tradingService.trade(req))
                .expectNextCount(1)
                .verifyComplete();

        // THEN
        verify(walletService, times(2)).save(walletCaptor.capture());
        var savedWallets = walletCaptor.getAllValues();

        // Check ETH (Debit)
        Wallet savedEth = savedWallets.stream().filter(w -> w.getCurrency().equals("ETH")).findFirst().get();
        // Should be 2.00 - 0.5 = 1.5.
        assertEquals(0, new BigDecimal("1.5").compareTo(savedEth.getBalance()), "ETH should decrease by Quantity (0.5)");
        // Check USDT (Credit)
        Wallet savedUsdt = savedWallets.stream().filter(w -> w.getCurrency().equals("USDT")).findFirst().get();
        assertEquals(0, new BigDecimal("2500.00").compareTo(savedUsdt.getBalance()), "USDT should increase by Value (1500)");
    }

    @Test
    void testDeadlockPrevention_SavesLowerIdFirst() {
        // GIVEN
        TradingRequest req = new TradingRequest(1L, "ETHUSDT", "BUY", new BigDecimal("1.0"));
        // ID 200 (USDT) vs ID 100 (ETH)
        // save ETH (100) first because 100 < 200.
        Wallet highIdWallet = createWallet(200L, "USDT", "5000.00");
        Wallet lowIdWallet = createWallet(100L, "ETH", "0.00");

        when(priceService.bestPrice(any(), any())).thenReturn(Mono.just(new BigDecimal("100.00")));
        when(walletService.findByUserAndCurrency(1L, "USDT")).thenReturn(Mono.just(highIdWallet));
        when(walletService.findByUserAndCurrency(1L, "ETH")).thenReturn(Mono.just(lowIdWallet));
        when(walletService.save(any(Wallet.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
        when(transactionRepo.save(any())).thenReturn(Mono.just(new TradingTransaction()));

        // WHEN
        tradingService.trade(req).block();

        InOrder inOrder = inOrder(walletService);

        // save ID 100 first, then ID 200
        inOrder.verify(walletService).save(argThat(w -> w.getId().equals(100L)));
        inOrder.verify(walletService).save(argThat(w -> w.getId().equals(200L)));
    }

    @Test
    void testInsufficientBalance_ThrowsError() {
        TradingRequest req = new TradingRequest(1L, "ETHUSDT", "BUY", new BigDecimal("1.0"));
        Wallet poorWallet = createWallet(1L, "USDT", "10.00"); // Only have 10
        Wallet ethWallet = createWallet(2L, "ETH", "0.00");

        when(priceService.bestPrice(any(), any())).thenReturn(Mono.just(new BigDecimal("2000.00")));
        when(walletService.findByUserAndCurrency(1L, "USDT")).thenReturn(Mono.just(poorWallet));
        when(walletService.findByUserAndCurrency(1L, "ETH")).thenReturn(Mono.just(ethWallet));

        StepVerifier.create(tradingService.trade(req))
                .expectErrorMatches(t -> t instanceof IllegalArgumentException && t.getMessage().contains("Insufficient USDT"))
                .verify();

        verify(walletService, never()).save(any());
    }
}