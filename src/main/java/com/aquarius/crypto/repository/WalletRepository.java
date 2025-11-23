package com.aquarius.crypto.repository;

import com.aquarius.crypto.model.Wallet;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface WalletRepository extends R2dbcRepository<Wallet, Long> {
    Flux<Wallet> findByUserId(Long userId);

    Mono<Wallet> findByUserIdAndCurrency(Long userId, String currency);

    Mono<Wallet> save(Wallet any);
}
