package com.aquarius.crypto.model;

import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "wallets")
public class Wallet {
    @Id
    private Long id;
    private Long userId;
    private String currency;
    private BigDecimal balance;
    private Instant createdAt;
    private Instant updatedAt;
}