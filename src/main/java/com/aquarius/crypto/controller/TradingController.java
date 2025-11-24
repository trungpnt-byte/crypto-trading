package com.aquarius.crypto.controller;

import com.aquarius.crypto.common.LocalApiResponse;
import com.aquarius.crypto.common.LocalPaginatedResponse;
import com.aquarius.crypto.dto.request.TradingRequest;
import com.aquarius.crypto.dto.response.TradingHistoryResponse;
import com.aquarius.crypto.model.TradingTransaction;
import com.aquarius.crypto.service.TradingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;


@RestController
@RequestMapping("/api/v1")
public class TradingController {

    private final TradingService tradingService;

    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @PreAuthorize("hasRole('TRADER')")
    @GetMapping("/users/{userId}/trades")
    public Mono<ResponseEntity<LocalPaginatedResponse<TradingHistoryResponse>>> getTradingHistoryPaginated(@PathVariable Long userId, @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "10") int size) {
        return tradingService.getUserTradingHistoryPaginated(userId, page, size)
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasRole('TRADER')")
    @GetMapping("/users/{userId}/trades/all")
    public Mono<ResponseEntity<LocalApiResponse<List<TradingHistoryResponse>>>> getTradingHistory(@PathVariable Long userId) {

        return tradingService.getUserTradingHistory(userId)
                .collectList()
                .map(historyList -> ResponseEntity.ok(
                        LocalApiResponse.success(historyList, "Trade history retrieved", 200)
                ));
    }

    @PreAuthorize("hasRole('TRADER')")
    @PostMapping("/trades")
    public Mono<ResponseEntity<LocalApiResponse<TradingTransaction>>> executeTrade(@RequestBody Mono<TradingRequest> requestMono // 1. Wrap in Mono
    ) {
        return requestMono
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Request body is mandatory")))
                .doOnNext(TradingRequest::validate)
                .flatMap(tradingService::trade)
                .map(tx -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(LocalApiResponse.success(
                                tx,
                                "Trade executed successfully (" + tx.getTradeType() + " " + tx.getSymbol() + ")",
                                HttpStatus.CREATED.value()
                        ))
                )
                .onErrorResume(e -> {
                    return Mono.just(
                            ResponseEntity
                                    .status(HttpStatus.BAD_REQUEST)
                                    .body(LocalApiResponse.error(
                                            e.getMessage(), // Will now catch "Request body is mandatory"
                                            HttpStatus.BAD_REQUEST.value()
                                    ))
                    );
                });
    }


}