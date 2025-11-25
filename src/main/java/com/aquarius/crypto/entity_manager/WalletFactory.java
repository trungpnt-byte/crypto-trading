package com.aquarius.crypto.entity_manager;

import com.aquarius.crypto.model.Wallet;

import java.math.BigDecimal;

public class WalletFactory {

    public static final WalletFactory INSTANCE = new WalletFactory();

    private WalletFactory() {
    }

    public Wallet createNewWallet(Long userId, String currency) {
        return Wallet.builder()
                .userId(userId)
                .currency(currency)
                .balance(BigDecimal.ZERO)
                .version(0L)
                .build();
    }

}
