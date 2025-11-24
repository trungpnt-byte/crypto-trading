package com.aquarius.crypto.service;


import com.aquarius.crypto.common.LocalPaginatedResponse;
import com.aquarius.crypto.dto.TradeType;
import com.aquarius.crypto.dto.request.TradingRequest;
import com.aquarius.crypto.dto.response.TradingHistoryResponse;
import com.aquarius.crypto.model.TradingTransaction;
import com.aquarius.crypto.model.Wallet;
import com.aquarius.crypto.repository.TradingTransactionRepository;
import com.aquarius.crypto.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradingService {

    private final WalletService walletService;
    private final TradingTransactionRepository transactionRepo;
    private final PriceAggregationService priceService;
    private final TransactionalOperator rxtx;

    @Value("${app.trading.auto-create-wallet:false}")
    private boolean autoCreateWallets;

    public Mono<TradingTransaction> trade(TradingRequest req) {
        String quote = "USDT";
        String base = req.getSymbol().replace(quote, "");

        return priceService.bestPrice(req.getSymbol(), req.getTradeType())
                .flatMap(price ->
                        executeTrade(req, price, base, quote)
                                .as(rxtx::transactional)
                )
                .retryWhen(Retry.backoff(3, Duration.ofMillis(100))
                        .filter(throwable -> throwable instanceof OptimisticLockingFailureException)
                );
    }

    private Mono<TradingTransaction> executeTrade(
            TradingRequest req,
            BigDecimal price,
            String baseCurrency,
            String quoteCurrency
    ) {
        boolean isBuy = req.getTradeType().equalsIgnoreCase("BUY");
        String debitCurrency = isBuy ? quoteCurrency : baseCurrency;
        String creditCurrency = isBuy ? baseCurrency : quoteCurrency;

        return Mono.zip(
                resolveWallet(req.getUserId(), debitCurrency),
                resolveWallet(req.getUserId(), creditCurrency)
        ).flatMap(tuple -> {
            Wallet debitWallet = tuple.getT1();
            Wallet creditWallet = tuple.getT2();
            return performAtomicUpdate(debitWallet, creditWallet, req, price, isBuy);
        });
    }

    /**
     * Logic: Find existing wallet. If missing, check toggle.
     */
    private Mono<Wallet> resolveWallet(Long userId, String currency) {
        return walletService.findByUserAndCurrency(userId, currency)
                .switchIfEmpty(Mono.defer(() -> {
                    if (this.autoCreateWallets) {
                        return createNewWallet(userId, currency);
                    } else {
                        return Mono.error(new IllegalArgumentException(
                                "Wallet not found for currency: " + currency + ". Auto-creation is disabled."
                        ));
                    }
                }));
    }

    private Mono<Wallet> createNewWallet(Long userId, String currency) {
        log.info("Auto-creating new wallet for User: {} Currency: {}", userId, currency);
        Wallet newWallet = Wallet.builder()
                .userId(userId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .version(0L)
                .build();
        return walletService.save(newWallet);
    }

    private Mono<TradingTransaction> saveTransactionAudit(
            TradingRequest req,
            BigDecimal price,
            BigDecimal totalAmount,
            String status
    ) {
        TradingTransaction tx = TradingTransaction.builder()
                .userId(req.getUserId())
                .symbol(req.getSymbol().toUpperCase())
                .tradeType(TradeType.valueOf(req.getTradeType().toUpperCase()))
                .quantity(req.getQuantity())
                .price(price)
                .totalAmount(totalAmount)
                .status(status)
                .createdAt(Instant.now())
                .build();

        return transactionRepo.save(tx);
    }

    private Mono<TradingTransaction> performAtomicUpdate(
            Wallet debit,
            Wallet credit,
            TradingRequest req,
            BigDecimal price,
            boolean isBuy
    ) {
        BigDecimal amountToDebit;
        BigDecimal amountToCredit;
        BigDecimal totalValueUsdt = price.multiply(req.getQuantity());

        if (isBuy) {
            amountToDebit = totalValueUsdt;
            amountToCredit = req.getQuantity();
        } else {
            amountToDebit = req.getQuantity();
            amountToCredit = totalValueUsdt;
        }

        if (debit.getBalance().compareTo(amountToDebit) < 0) {
            return Mono.error(new IllegalArgumentException("Insufficient " + debit.getCurrency() + " balance"));
        }

        Wallet updatedDebit = debit.toBuilder()
                .balance(debit.getBalance().subtract(amountToDebit))
                .build();

        Wallet updatedCredit = credit.toBuilder()
                .balance(credit.getBalance().add(amountToCredit))
                .build();

        Mono<Wallet> saveFlow;
        if (updatedDebit.getId() < updatedCredit.getId()) {
            saveFlow = walletService.save(updatedDebit).then(walletService.save(updatedCredit));
        } else {
            saveFlow = walletService.save(updatedCredit).then(walletService.save(updatedDebit));
        }

        return saveFlow
                .flatMap(ignore -> saveTransactionAudit(req, price, totalValueUsdt, "COMPLETED"))
                .onErrorResume(e -> {
                    log.error("Trade failed for user {}: {}", req.getUserId(), e.getMessage());
                    return Mono.error(e);
                });
    }


    public Mono<LocalPaginatedResponse<TradingHistoryResponse>> getUserTradingHistoryPaginated(Long userId, int page, int size) {
        int offset = page * size;
        Mono<Long> totalCount = transactionRepo.findByUserId(userId).count();

        Flux<TradingHistoryResponse> pagedTradingHistoryFlux = transactionRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .skip(offset)
                .take(size)
                .map(TradingHistoryResponse::fromEntity);
        return totalCount.zipWith(pagedTradingHistoryFlux.collectList(),
                (total, list) -> LocalPaginatedResponse.<TradingHistoryResponse>builder()
                        .contents(list)
                        .totalItems(total)
                        .page(page)
                        .size(size)
                        .totalPages((int) Math.ceil((double) total / size))
                        .build()
        );
    }


    public Flux<TradingHistoryResponse> getUserTradingHistory(Long userId) {
        return transactionRepo.findByUserIdOrderByCreatedAtDesc(userId)
                .map(TradingHistoryResponse::fromEntity);
    }

}
