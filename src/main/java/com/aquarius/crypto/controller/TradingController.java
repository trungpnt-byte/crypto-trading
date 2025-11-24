package com.aquarius.crypto.controller;

import com.aquarius.crypto.common.LocalApiResponse;
import com.aquarius.crypto.common.LocalPaginatedResponse;
import com.aquarius.crypto.dto.request.TradingRequest;
import com.aquarius.crypto.dto.response.TradingHistoryResponse;
import com.aquarius.crypto.model.TradingTransaction;
import com.aquarius.crypto.service.SecurityContextService;
import com.aquarius.crypto.service.TradingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/v1")
public class TradingController {

    private final TradingService tradingService;
    private final SecurityContextService securityContextService;

    public TradingController(TradingService tradingService, SecurityContextService securityContextService) {
        this.tradingService = tradingService;
        this.securityContextService = securityContextService;
    }

    @PreAuthorize("hasRole('TRADER')")
    @GetMapping("/users/trades/me")
    public Mono<ResponseEntity<LocalPaginatedResponse<TradingHistoryResponse>>> getTradingHistoryPaginated(@RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "10") int size) {
        return securityContextService.getInternalUserId()
                .flatMap(internalUserId -> tradingService.getUserTradingHistoryPaginated(internalUserId, page, size))
                .map(ResponseEntity::ok);
    }

    @PreAuthorize("hasRole('TRADER')")
    @GetMapping("/users/trades/me/all")
    public Mono<ResponseEntity<LocalApiResponse<List<TradingHistoryResponse>>>> getTradingHistory() {
        return securityContextService.getInternalUserId()
                .flatMapMany(tradingService::getUserTradingHistory)
                .collectList()
                .map(historyList -> ResponseEntity.ok(
                        LocalApiResponse.success(historyList, "Trade history retrieved", 200)
                ));
    }

    @PreAuthorize("hasRole('TRADER')")
    @PostMapping("/trades")
    public Mono<ResponseEntity<LocalApiResponse<TradingTransaction>>> executeTrade(@RequestBody Mono<TradingRequest> requestMono) {
        return Mono.zip(securityContextService.getInternalUserId(), requestMono)
                .flatMap(tuple -> {
                    Long internalUserId = tuple.getT1();
                    TradingRequest tradingRequest = tuple.getT2();
                    // Override userId from payload
                    tradingRequest.setUserId(internalUserId);
                    tradingRequest.validate();
                    return tradingService.trade(tradingRequest)
                            .map(tx -> ResponseEntity
                                    .status(HttpStatus.CREATED)
                                    .body(LocalApiResponse.success(
                                            tx,
                                            "Trade executed successfully (" + tx.getTradeType() + " " + tx.getSymbol() + ")",
                                            HttpStatus.CREATED.value()
                                    ))
                            );
                }).onErrorResume(e -> Mono.just(
                        ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(LocalApiResponse.error(
                                        e.getMessage(),
                                        HttpStatus.BAD_REQUEST.value()
                                ))
                ));
    }

}