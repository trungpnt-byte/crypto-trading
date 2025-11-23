package com.aquarius.crypto.controller;

import com.aquarius.crypto.common.LocalApiResponse;
import com.aquarius.crypto.dto.response.WalletBalanceResponse;
import com.aquarius.crypto.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/balance")
    public Mono<ResponseEntity<LocalApiResponse<List<WalletBalanceResponse>>>> getWalletBalances(
            Authentication authentication) {

        Long userId = (Long) authentication.getPrincipal();

        return walletService.getUserWalletBalances(userId)
                .collectList()
                .map(balances -> ResponseEntity.ok(LocalApiResponse.success(balances,
                        "Wallet balances retrieved successfully",
                        200)));
    }
}

