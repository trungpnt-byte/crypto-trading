package com.aquarius.crypto.service;

import com.aquarius.crypto.common.LocalPaginatedResponse;
import com.aquarius.crypto.dto.response.WalletBalanceResponse;
import com.aquarius.crypto.model.Wallet;
import com.aquarius.crypto.repository.WalletRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Service
public class WalletService {

    private final SecurityContextService securityContextService;
    private final WalletRepository walletRepository;

    public WalletService(SecurityContextService securityContextService, WalletRepository walletRepository) {
        this.securityContextService = securityContextService;
        this.walletRepository = walletRepository;
    }

    public Mono<Wallet> findByUserAndCurrency(Long userId, String debitCurrency) {
        return walletRepository.findByUserAndCurrency(userId, debitCurrency);
    }

    public Flux<WalletBalanceResponse> getUserWalletBalances() {
        return securityContextService.getInternalUserId()
                .flatMapMany(internalId -> walletRepository.findByUserId(internalId)
                        .map(WalletBalanceResponse::fromEntity))
                .switchIfEmpty(Flux.error(new UsernameNotFoundException(
                        "No wallet balances found for the authenticated user.")));
    }

    public Mono<LocalPaginatedResponse<WalletBalanceResponse>> getUserWalletsPaginated(int page, int size) {
        Mono<Long> internalIdMono = securityContextService.getInternalUserId();
        int offset = page * size;
        return internalIdMono.flatMap(internalId -> {
                    Mono<Long> totalCount = walletRepository.findByUserId(internalId).count();
                    Mono<List<WalletBalanceResponse>> pagedListMono = walletRepository.findByUserId(internalId)
                            .skip(offset)
                            .take(size)
                            .map(WalletBalanceResponse::fromEntity)
                            .collectList();
                    return totalCount.zipWith(pagedListMono);
                }).<LocalPaginatedResponse<WalletBalanceResponse>>handle((tuple, sink) -> {
                    long total = tuple.getT1();
                    List<WalletBalanceResponse> list = tuple.getT2();

                    if (list.isEmpty() && total == 0) {
                        sink.error(new UsernameNotFoundException("No wallet data found for authenticated user."));
                        return;
                    }

                    sink.next(LocalPaginatedResponse.<WalletBalanceResponse>builder()
                            .contents(list)
                            .page(page)
                            .size(size)
                            .totalItems(total)
                            .totalPages((int) Math.ceil((double) total / size))
                            .build());
                })
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User identity could not be resolved or mapped.")));
    }

    public Mono<Wallet> save(Wallet toPersist) {
        toPersist.setUpdatedAt(Instant.now());
        return walletRepository.save(toPersist);
    }
}
