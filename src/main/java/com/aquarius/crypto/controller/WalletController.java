package com.aquarius.crypto.controller;

import com.aquarius.crypto.common.LocalApiResponse;
import com.aquarius.crypto.common.LocalPaginatedResponse;
import com.aquarius.crypto.dto.response.WalletBalanceResponse;
import com.aquarius.crypto.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PreAuthorize("hasRole('TRADER')")
    @GetMapping("/users/wallets/me")
    public Mono<ResponseEntity<LocalPaginatedResponse<WalletBalanceResponse>>> getUserWalletsPaginated(@RequestParam(name = "page", defaultValue = "0") int page, @RequestParam(name = "size", defaultValue = "10") int size) {
        return walletService.getUserWalletsPaginated(page, size).map(ResponseEntity::ok);
    }

    @PreAuthorize("hasRole('TRADER')")
    @GetMapping("/users/wallets/me/all")
    public Mono<ResponseEntity<LocalApiResponse<List<WalletBalanceResponse>>>> getWalletBalances() {

        return walletService.getUserWalletBalances()
                .collectList()
                .map(balances -> ResponseEntity.ok(
                        LocalApiResponse.success(balances, "Wallet balances retrieved", 200)
                ));
    }
}

