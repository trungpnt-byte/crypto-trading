package com.aquarius.crypto.repository;

import com.aquarius.crypto.model.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByUsername(String username);

    Mono<User> findByPublicId(UUID publicId);

    Mono<User> findByEmail(String email);

    Flux<User> findAll();
}
