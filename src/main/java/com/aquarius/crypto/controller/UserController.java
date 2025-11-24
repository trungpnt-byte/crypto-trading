package com.aquarius.crypto.controller;

import com.aquarius.crypto.dto.UserResponseModel;
import com.aquarius.crypto.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trade/v1")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('TRADER')")
    @GetMapping("/users/by-email")
    public UserResponseModel findByEmail(@RequestParam("email") String email) {
        UserResponseModel result = userService.findByEmail(email)
                .map(user -> new UserResponseModel(
                        user.getUsername(),
                        user.getEmail()
                )).block();
        return result;
    }
}
