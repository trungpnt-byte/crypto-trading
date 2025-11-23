package com.aquarius.crypto.service;

import com.aquarius.crypto.model.User;
import com.aquarius.crypto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final JwtService jwtService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(BCryptPasswordEncoder.BCryptVersion.$2Y, 10);

    public UserService(UserRepository userRepository, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public User register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    public String verify(User user) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        user.getUsername(),
                        user.getPassword()
                )
        );

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(user.getUsername());
        }

        return "Failed";
    }
}