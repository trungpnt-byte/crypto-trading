package com.aquarius.crypto.repository;

import com.aquarius.crypto.model.PriceAggregation;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PriceAggregationRepository extends R2dbcRepository<PriceAggregation, Long> {
    @Query(value = "SELECT * FROM price_aggregations WHERE trading_pair = :tradingPair ORDER BY created_at DESC LIMIT 1")
    Mono<PriceAggregation> findLatestByTradingPair(@Param("tradingPair") String tradingPair);
}
