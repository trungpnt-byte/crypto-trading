package com.aquarius.crypto.service;


import com.aquarius.crypto.dto.request.TradeRequest;
import com.aquarius.crypto.dto.response.TradeResponse;
import com.aquarius.crypto.exception.InsufficientBalanceException;
import com.aquarius.crypto.exception.PriceNotFoundException;
import com.aquarius.crypto.model.PriceAggregation;
import com.aquarius.crypto.model.TradingTransaction;
import com.aquarius.crypto.model.Wallet;
import com.aquarius.crypto.repository.PriceAggregationRepository;
import com.aquarius.crypto.repository.TradingTransactionRepository;
import com.aquarius.crypto.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradingService {

    private final TradingTransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final PriceAggregationRepository priceRepository;

    @Transactional
    public Mono<TradeResponse> executeTrade(Long userId, TradeRequest request) {
        try {
            Mono<PriceAggregation> priceMono = priceRepository.findLatestByTradingPair(request.getTradingPair());
            return priceMono
                    .switchIfEmpty(Mono.error(new PriceNotFoundException("No price available for " + request.getTradingPair())))
                    .flatMap(price -> {
                        BigDecimal executionPrice = "BUY".equals(request.getTradeType())
                                ? price.getBestAskPrice()
                                : price.getBestBidPrice();

                        BigDecimal totalAmount = request.getQuantity()
                                .multiply(executionPrice)
                                .setScale(8, RoundingMode.HALF_UP);

                        return executeTrade(userId, request, executionPrice, totalAmount);
                    });
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private Mono<TradeResponse> executeTrade(Long userId, TradeRequest request,
                                             BigDecimal price, BigDecimal totalAmount) {
        String baseCurrency = request.getTradingPair().replace("USDT", "");

        if ("BUY".equals(request.getTradeType())) {
            return processBuyOrder(userId, request, baseCurrency, price, totalAmount);
        } else {
            return processSellOrder(userId, request, baseCurrency, price, totalAmount);
        }
    }

    private Mono<TradeResponse> processBuyOrder(Long userId, TradeRequest request,
                                                String baseCurrency, BigDecimal price,
                                                BigDecimal totalAmount) {
        return walletRepository.findByUserIdAndCurrency(userId, "USDT")
                .switchIfEmpty(Mono.error(new InsufficientBalanceException("USDT wallet not found")))
                .flatMap(usdtWallet -> {
                    if (usdtWallet.getBalance().compareTo(totalAmount) < 0) {
                        return Mono.error(new InsufficientBalanceException(
                                "Insufficient USDT balance. Required: " + totalAmount +
                                        ", Available: " + usdtWallet.getBalance()));
                    }

                    usdtWallet.setBalance(usdtWallet.getBalance().subtract(totalAmount));
                    usdtWallet.setUpdatedAt(Instant.now());

                    return walletRepository.save(usdtWallet)
                            .then(walletRepository.findByUserIdAndCurrency(userId, baseCurrency))
                            .switchIfEmpty(createWallet(userId, baseCurrency))
                            .flatMap(cryptoWallet -> {
                                cryptoWallet.setBalance(cryptoWallet.getBalance().add(request.getQuantity()));
                                cryptoWallet.setUpdatedAt(Instant.now());
                                return walletRepository.save(cryptoWallet);
                            })
                            .then(createTransaction(userId, request, price, totalAmount, "COMPLETED"));
                });
    }

    private Mono<TradeResponse> processSellOrder(Long userId, TradeRequest request,
                                                 String baseCurrency, BigDecimal price,
                                                 BigDecimal totalAmount) {
        return walletRepository.findByUserIdAndCurrency(userId, baseCurrency)
                .switchIfEmpty(Mono.error(new InsufficientBalanceException(baseCurrency + " wallet not found")))
                .flatMap(cryptoWallet -> {
                    if (cryptoWallet.getBalance().compareTo(request.getQuantity()) < 0) {
                        return Mono.error(new InsufficientBalanceException(
                                "Insufficient " + baseCurrency + " balance. Required: " +
                                        request.getQuantity() + ", Available: " + cryptoWallet.getBalance()));
                    }

                    cryptoWallet.setBalance(cryptoWallet.getBalance().subtract(request.getQuantity()));
                    cryptoWallet.setUpdatedAt(Instant.now());

                    return walletRepository.save(cryptoWallet)
                            .then(walletRepository.findByUserIdAndCurrency(userId, "USDT"))
                            .flatMap(usdtWallet -> {
                                usdtWallet.setBalance(usdtWallet.getBalance().add(totalAmount));
                                usdtWallet.setUpdatedAt(Instant.now());
                                return walletRepository.save(usdtWallet);
                            })
                            .then(createTransaction(userId, request, price, totalAmount, "COMPLETED"));
                });
    }

    private Mono<Wallet> createWallet(Long userId, String currency) {
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return walletRepository.save(wallet);
    }

    private Mono<TradeResponse> createTransaction(Long userId, TradeRequest request,
                                                  BigDecimal price, BigDecimal totalAmount,
                                                  String status) {
        TradingTransaction transaction = TradingTransaction.builder()
                .userId(userId)
                .tradingPair(request.getTradingPair())
                .tradeType(request.getTradeType())
                .quantity(request.getQuantity())
                .price(price)
                .totalAmount(totalAmount)
                .status(status)
                .createdAt(Instant.now())
                .build();

        return transactionRepository.save(transaction)
                .map(saved -> TradeResponse.builder()
                        .transactionId(saved.getId())
                        .tradingPair(saved.getTradingPair())
                        .tradeType(saved.getTradeType())
                        .quantity(saved.getQuantity())
                        .price(saved.getPrice())
                        .totalAmount(saved.getTotalAmount())
                        .status(saved.getStatus())
                        .timestamp(saved.getCreatedAt())
                        .build());
    }

    public Flux<TradeResponse> getUserTradingHistory(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .map(tx -> TradeResponse.builder()
                        .transactionId(tx.getId())
                        .tradingPair(tx.getTradingPair())
                        .tradeType(tx.getTradeType())
                        .quantity(tx.getQuantity())
                        .price(tx.getPrice())
                        .totalAmount(tx.getTotalAmount())
                        .status(tx.getStatus())
                        .timestamp(tx.getCreatedAt())
                        .build());
    }
}

