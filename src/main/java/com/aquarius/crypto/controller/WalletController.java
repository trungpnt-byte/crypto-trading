package com.aquarius.crypto.controller;

import com.aquarius.crypto.common.LocalApiResponse;
import com.aquarius.crypto.common.LocalPaginatedResponse;
import com.aquarius.crypto.dto.response.WalletBalanceResponse;
import com.aquarius.crypto.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PreAuthorize("hasRole('TRADER')")
    @GetMapping
    public Mono<ResponseEntity<LocalPaginatedResponse<WalletBalanceResponse>>> getUserWalletsPaginated(@PathVariable Long userId, @RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "10") int size) {
        return walletService.getUserWalletsPaginated(userId, page, size).map(ResponseEntity::ok);
    }

    @PreAuthorize("hasRole('TRADER')")
    @GetMapping("/users/{userId}/wallets/all")
    public Mono<ResponseEntity<LocalApiResponse<List<WalletBalanceResponse>>>> getWalletBalances(
            @PathVariable Long userId) {

        return walletService.getUserWalletBalances(userId)
                .collectList()
                .map(balances -> ResponseEntity.ok(
                        LocalApiResponse.success(balances, "Wallet balances retrieved", 200)
                ));
    }
}

