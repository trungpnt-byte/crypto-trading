package com.aquarius.crypto.service;

import com.aquarius.crypto.model.User;
import com.aquarius.crypto.repository.UserRepository;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    private final UserRepository userRepository;

    private final ReactiveAuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, JwtService jwtService, ReactiveAuthenticationManager authenticationManager, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.encoder = encoder;
    }

    public Mono<User> findByPublicId(String username) {
        return userRepository.findByUsername(username);
    }

    public Mono<User> registerUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepository.save(user);
    }


    public Mono<User> createUser(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Flux<User> findAll() {
        return userRepository.findAll();
    }
}