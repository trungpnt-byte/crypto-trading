package com.aquarius.crypto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "wallets")
@Entity
public class Wallet {
    @Id
    private Long id;
    private Long userId;
    private String currency;
    private BigDecimal balance;
    private Instant createdAt;
    private Instant updatedAt;
}