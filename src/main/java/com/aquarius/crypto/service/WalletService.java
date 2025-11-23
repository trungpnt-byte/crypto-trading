package com.aquarius.crypto.service;

import com.aquarius.crypto.dto.response.WalletBalanceResponse;
import com.aquarius.crypto.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;

    public Flux<WalletBalanceResponse> getUserWalletBalances(Long userId) {
        return walletRepository.findByUserId(userId)
                .map(wallet -> WalletBalanceResponse.builder()
                        .currency(wallet.getCurrency())
                        .balance(wallet.getBalance())
                        .build());
    }
}
