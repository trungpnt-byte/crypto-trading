package com.aquarius.crypto.repository;

import com.aquarius.crypto.model.TradingTransaction;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TradingTransactionRepository extends R2dbcRepository<TradingTransaction, Long> {
    Flux<TradingTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    Flux<TradingTransaction> findByUserId(Long userId);
}
