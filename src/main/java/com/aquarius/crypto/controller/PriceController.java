package com.aquarius.crypto.controller;

import com.aquarius.crypto.common.LocalApiResponse;
import com.aquarius.crypto.dto.response.AggregatedPriceResponse;
import com.aquarius.crypto.service.PriceAggregationService;
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
    @GetMapping("/prices/{symbol}/latest")
    public Mono<ResponseEntity<LocalApiResponse<AggregatedPriceResponse>>> getLatestPrice(
            @PathVariable("symbol") String tradingPair) {

        return priceService.findLatestByTradingPair(tradingPair)
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
