package com.aquarius.crypto.dao;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserDao implements ReactiveUserDetailsService {

    private final static List<UserDetails> APPLICATION_USERS = List.of(
            // NOTE: Passwords must be encoded (using {bcrypt} prefix for clarity)
            new User("abc", "{bcrypt}123", Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))),
            new User("admin@test.com", "{bcrypt}$2a$12$hhUp7igUmXnd2RUWRmoG5uY1df2Ceo6tvzkgJWfdsTt3BkhzhZhqC", Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"))),
            new User("user@test.com", "{bcrypt}$2a$12$hhUp7igUmXnd2RUWRmoG5uY1df2Ceo6tvzkgJWfdsTt3BkhzhZhqC", Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))),
            new User("trader1", "{bcrypt}$2a$12$hhUp7igUmXnd2RUWRmoG5uY1df2Ceo6tvzkgJWfdsTt3BkhzhZhqC", Collections.singleton(new SimpleGrantedAuthority("TRADER"))),
            new User("trader2", "{bcrypt}$2a$12$hhUp7igUmXnd2RUWRmoG5uY1df2Ceo6tvzkgJWfdsTt3BkhzhZhqC", Collections.singleton(new SimpleGrantedAuthority("TRADER")))
    );

    private final static ConcurrentHashMap<String, UserDetails> users = new ConcurrentHashMap<>() {{
        for (UserDetails userDetails : APPLICATION_USERS) {
            put(userDetails.getUsername(), userDetails);
        }
    }};

    @NotNull
    @Override
    public Mono<UserDetails> findByUsername(@NotNull String email) {
        return Mono.fromCallable(() -> this.findUserByEmail(email))
                .subscribeOn(Schedulers.boundedElastic());
    }


    public UserDetails findUserByEmail(String email) {
        UserDetails userDetails = users.get(email);
        if (userDetails == null) {
            throw new UsernameNotFoundException("User with email " + email + " not found");
        }
        return userDetails;
    }
}