package com.aquarius.crypto.controller;

import com.aquarius.crypto.dto.UserResponseModel;
import com.aquarius.crypto.repository.UserRepository;
import com.aquarius.crypto.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trade/v1/users")
public class UserController {
    private final UserRepository userRepository;
    private final UserService userService;

    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PreAuthorize("hasRole('TRADER')")
    @GetMapping
    public UserResponseModel findByEmail(@RequestParam("email") String email) {
        return userService.findByEmail(email)
                .map(user -> new UserResponseModel(
                        user.getUsername(),
                        user.getEmail()
                )).block();
    }
}
