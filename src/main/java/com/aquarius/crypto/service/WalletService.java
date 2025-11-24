package com.aquarius.crypto.service;

import com.aquarius.crypto.dto.response.WalletBalanceResponse;
import com.aquarius.crypto.model.Wallet;
import com.aquarius.crypto.repository.WalletRepository;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Flux<WalletBalanceResponse> getUserWalletBalances(Long userId) {
        return walletRepository.findByUserId(userId)
                .map(wallet -> WalletBalanceResponse.builder()
                        .currency(wallet.getCurrency())
                        .balance(wallet.getBalance())
                        .build());
    }

    public Mono<Wallet> findByUserAndCurrency(Long userId, String debitCurrency) {
        return walletRepository.findByUserAndCurrency(userId, debitCurrency);
    }
}
