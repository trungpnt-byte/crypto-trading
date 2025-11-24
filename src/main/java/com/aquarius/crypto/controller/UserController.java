package com.aquarius.crypto.controller;

import com.aquarius.crypto.common.LocalApiResponse;
import com.aquarius.crypto.dto.UserResponseModel;
import com.aquarius.crypto.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('TRADER')")
    @GetMapping("/users/name")
    public Mono<LocalApiResponse<UserResponseModel>> findByName(String userName) {
        return userService.findByEmail(userName)
                .map(user -> {
                    UserResponseModel responseModel = UserResponseModel.builder()
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .build();
                    return LocalApiResponse.success(responseModel, "User found", 200);
                });
    }
}
