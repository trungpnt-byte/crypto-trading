package com.aquarius.crypto.controller;

import com.aquarius.crypto.common.LocalApiResponse;
import com.aquarius.crypto.config.tenant.TenantContext;
import com.aquarius.crypto.dto.response.AggregatedPriceResponse;
import com.aquarius.crypto.service.PriceAggregationService;
import com.aquarius.crypto.service.SecurityContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PriceController {

    private final PriceAggregationService priceService;

    @PreAuthorize("hasRole('TRADER')")
    @GetMapping("/prices/{trading-pair}")
    public Mono<ResponseEntity<LocalApiResponse<AggregatedPriceResponse>>> getLatestPrice(
            @PathVariable String tradingPair) {

        return priceService.findByTradingPair(tradingPair)
                .map(aggregatedPriceResponse -> ResponseEntity.ok(
                        LocalApiResponse.success(
                                aggregatedPriceResponse,
                                "Price retrieved successfully",
                                HttpStatus.OK.value()
                        )
                ))
                .defaultIfEmpty(
                        ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                                LocalApiResponse.error(
                                        "Price not found for trading pair " + tradingPair,
                                        HttpStatus.NOT_FOUND.value()
                                )
                        )
                );
    }
}
