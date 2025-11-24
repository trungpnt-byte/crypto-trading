package com.aquarius.crypto.model;

import lombok.*;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Table("wallets")
public class Wallet {
    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    private String currency;

    private BigDecimal balance;

    @Version
    private Long version;
    
    @Column("created_at")
    private Instant createdAt;

    @Column("updated_at")
    private Instant updatedAt;
}