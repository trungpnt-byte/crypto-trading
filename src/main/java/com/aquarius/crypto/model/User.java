package com.aquarius.crypto.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "users")
@Entity
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;
}