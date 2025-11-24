package com.aquarius.crypto.dto.response;

import com.aquarius.crypto.model.Wallet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletBalanceResponse {
    private String currency;
    private BigDecimal balance;

    public static WalletBalanceResponse fromEntity(Wallet wallet) {
        return WalletBalanceResponse.builder()
                .currency(wallet.getCurrency())
                .balance(wallet.getBalance())
                .build();
    }
}
