package com.aquarius.crypto.service;

import com.aquarius.crypto.exception.UserNotFoundException;
import com.aquarius.crypto.model.User;
import com.aquarius.crypto.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {

    private final SecurityContextService securityContextService;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserService(SecurityContextService securityContextService, UserRepository userRepository, PasswordEncoder encoder) {
        this.securityContextService = securityContextService;
        this.userRepository = userRepository;
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

    public Mono<User> updateUserTimezone(String timezoneId) {
        return securityContextService.getInternalUserId()
                .flatMap(userId -> userRepository.findById(userId)
                        .flatMap(user -> {
                            user.setPreferredTimezone(timezoneId);
                            return userRepository.save(user);
                        }).switchIfEmpty(Mono.error(new UserNotFoundException("User not found."))));
    }
}