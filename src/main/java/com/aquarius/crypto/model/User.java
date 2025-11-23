package com.aquarius.crypto.model;

import org.springframework.data.relational.core.mapping.Table;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;
}