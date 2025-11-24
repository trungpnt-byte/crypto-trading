package com.aquarius.crypto.service;

import com.aquarius.crypto.common.LocalPaginatedResponse;
import com.aquarius.crypto.dto.response.TradingHistoryResponse;
import com.aquarius.crypto.dto.response.WalletBalanceResponse;
import com.aquarius.crypto.model.Wallet;
import com.aquarius.crypto.repository.WalletRepository;
import io.r2dbc.spi.Result;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public Mono<Wallet> findByUserAndCurrency(Long userId, String debitCurrency) {
        return walletRepository.findByUserAndCurrency(userId, debitCurrency);
    }

    /**
     * Retrieves all non-zero balances for a user.
     * Prod-Tip: Filter out zero balances to reduce payload size if desired.
     */
    public Flux<WalletBalanceResponse> getUserWalletBalances(Long userId) {
        return walletRepository.findByUserId(userId)
                .map(WalletBalanceResponse::fromEntity);
    }

    public Mono<LocalPaginatedResponse<WalletBalanceResponse>> getUserWalletsPaginated(Long userId, int page, int size) {
        int offset = page * size;
        Mono<Long> totalCount = walletRepository.findByUserId(userId).count();

        Flux<WalletBalanceResponse> pagedTradingHistoryFlux = walletRepository.findByUserId(userId)
                .skip(offset)
                .take(size)
                .map(WalletBalanceResponse::fromEntity);
        return totalCount.zipWith(pagedTradingHistoryFlux.collectList(),
                (total, list) -> LocalPaginatedResponse.<WalletBalanceResponse>builder()
                        .contents(list)
                        .page(page)
                        .size(size)
                        .totalItems(total)
                        .totalPages((int) Math.ceil((double) total / size))
                        .build()
        );
    }
}
