package com.aquarius.crypto.controller;

import com.aquarius.crypto.dto.UserResponseModel;
import com.aquarius.crypto.model.User;
import com.aquarius.crypto.repository.UserRepository;
import com.aquarius.crypto.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
        UserResponseModel result = userService.findByEmail(email)
                .map(user -> new UserResponseModel(
                        user.getUsername(),
                        user.getEmail()
                )).block();
        return result;
    }

    @PreAuthorize("hasRole('TRADER')")
    @GetMapping("/ping")
    public String ping() {
        User byUserName = userRepository.findByUsername("trader1").block();
        assert byUserName != null;
        List<User> users = userRepository.findAll().collectList().block();
        assert users != null;
        return users.toString();
    }
}
