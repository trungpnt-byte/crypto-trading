package com.aquarius.crypto.service;


import com.aquarius.crypto.dto.TradeType;
import com.aquarius.crypto.dto.request.TradingRequest;
import com.aquarius.crypto.model.TradingTransaction;
import com.aquarius.crypto.model.Wallet;
import com.aquarius.crypto.repository.TradingTransactionRepository;
import com.aquarius.crypto.repository.WalletRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;

@Service
public class TradingService {

    private static final int RETRIES = 5;
    private final WalletRepository walletRepo;
    private final WalletService walletService;
    private final TradingTransactionRepository tradingTransactionRepo;
    private final PriceAggregationService priceService;
    private final TransactionalOperator tx;

    public TradingService(WalletRepository walletRepo, WalletService walletService, TradingTransactionRepository tradingTransactionRepo, PriceAggregationService priceService, TransactionalOperator tx) {
        this.walletRepo = walletRepo;
        this.walletService = walletService;
        this.tradingTransactionRepo = tradingTransactionRepo;
        this.priceService = priceService;
        this.tx = tx;
    }

    public Mono<TradingTransaction> trade(TradingRequest req) {
        String base = req.getSymbol().substring(0, 3);
        String quote = "USDT";
        boolean isBuy = req.getTradeType().equalsIgnoreCase("BUY");
        String debitCurrency = isBuy ? quote : base;
        String creditCurrency = isBuy ? base : quote;

        return priceService.bestPrice(req.getSymbol(), req.getTradeType())
                .flatMap(price ->
                        Mono.defer(() -> executeTrade(req, price, debitCurrency, creditCurrency))
                )
                .as(tx::transactional)
                .retry(RETRIES);
    }

    private Mono<TradingTransaction> executeTrade(
            TradingRequest req,
            BigDecimal price,
            String debitCurrency,
            String creditCurrency
    ) {
        return Mono.defer(() ->
                walletService.findByUserAndCurrency(req.getUserId(), debitCurrency)
                        .zipWith(walletService.findByUserAndCurrency(req.getUserId(), creditCurrency))
                        .switchIfEmpty(Mono.error(new RuntimeException("Wallet not found")))
                        .flatMap(tuple -> performAtomicUpdate(tuple.getT1(), tuple.getT2(), req, price)));
    }

    private Mono<TradingTransaction> performAtomicUpdate(
            Wallet debit,
            Wallet credit,
            TradingRequest req,
            BigDecimal price
    ) {
        BigDecimal totalCost = price.multiply(req.getQuantity());

        if (debit.getBalance().compareTo(totalCost) < 0) {
            return Mono.error(new RuntimeException("Insufficient balance"));
        }
        Wallet updatedDebit = debit.toBuilder()
                .balance(debit.getBalance().subtract(totalCost))
                .build();

        Wallet updatedCredit = credit.toBuilder()
                .balance(credit.getBalance().add(req.getQuantity()))
                .build();

        return walletRepo.save(updatedDebit)
                .flatMap(savedDebit -> walletRepo.save(updatedCredit))
                .flatMap(savedCredit -> saveTrade(req, price))
                .onErrorResume(OptimisticLockingFailureException.class, e -> {
                    return Mono.error(new OptimisticLockingFailureException("[TradingService]Concurrent modification detected during trade"));
                });
    }

    private Mono<TradingTransaction> saveTrade(TradingRequest req, BigDecimal price) {

        TradingTransaction transaction = buildTransaction(req, price, price.multiply(req.getQuantity()), "COMPLETED");
        return tradingTransactionRepo.save(transaction);
    }

    private TradingTransaction buildTransaction(TradingRequest request, BigDecimal price, BigDecimal totalAmount, String status) {
        return TradingTransaction.builder()
                .userId(request.getUserId())
                .symbol(request.getSymbol().toUpperCase())
                .tradeType(TradeType.from(request.getTradeType()))
                .quantity(request.getQuantity())
                .price(price)
                .totalAmount(totalAmount)
                .status(status)
                .createdAt(Instant.now())
                .build();
    }

    //TODO move
    public Flux<Wallet> getWalletBalances(Long eq) {
        return null;
    }

    public Flux<TradingTransaction> getTradeHistory(Long userId) {
        return tradingTransactionRepo.findByUserId(userId);
    }
}

