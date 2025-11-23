package com.aquarius.crypto.controller;

import com.aquarius.crypto.common.LocalApiResponse;
import com.aquarius.crypto.dto.response.PriceResponse;
import com.aquarius.crypto.service.PriceAggregationService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceAggregationService priceService;

    @GetMapping("/{tradingPair}")
    public Mono<ResponseEntity<LocalApiResponse<PriceResponse>>> getLatestPrice(
            @PathVariable String tradingPair) {

        return priceService.getLatestPrice(tradingPair.toUpperCase())
                .map(price -> ResponseEntity.ok(LocalApiResponse.success(price,
                        "Price retrieved successfully",
                        200)));

    }
}
