package com.aquarius.crypto.repository;

import com.aquarius.crypto.model.Wallet;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface WalletRepository extends R2dbcRepository<Wallet, Long> {

    Flux<Wallet> findByUserId(Long userId);

    @Query("SELECT * FROM wallets WHERE user_id = $1 AND currency = $2")
    Mono<Wallet> findByUserAndCurrency(Long userId, String currency);


}
