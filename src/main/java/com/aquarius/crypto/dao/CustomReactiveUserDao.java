package com.aquarius.crypto.dao;

import com.aquarius.crypto.model.User;
import com.aquarius.crypto.model.UserPrincipal;
import com.aquarius.crypto.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

@Repository
public class CustomReactiveUserDao implements ReactiveUserDetailsService {
    private final UserRepository userRepository;

    public CustomReactiveUserDao(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)))
                .map(this::mapUserToPrincipal);
    }

    private UserPrincipal mapUserToPrincipal(User user) {
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getSimpleRole().toUpperCase())
        );
        return new UserPrincipal(
                user.getId(),
                user.getPublicId(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getTenantId()
        );
    }

}