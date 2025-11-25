package com.aquarius.crypto.controller;

import com.aquarius.crypto.common.LocalApiResponse;
import com.aquarius.crypto.dto.UserResponseModel;
import com.aquarius.crypto.dto.request.TimezoneUpdateRequest;
import com.aquarius.crypto.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.DateTimeException;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('TRADER')")
    @PutMapping("/users/me/timezone")
    public Mono<ResponseEntity<LocalApiResponse<UserResponseModel>>> updateTimezone(
            @RequestBody Mono<TimezoneUpdateRequest> requestMono) {

        return requestMono
                .flatMap(tuple -> {
                    String newTimezoneId = tuple.getTimezone();
                    try {
                        ZoneId.of(newTimezoneId);
                    } catch (DateTimeException e) {
                        return Mono.error(new IllegalArgumentException("Invalid Timezone ID format."));
                    }

                    // 2. Delegate to UserService to persist the change
                    return userService.updateUserTimezone(newTimezoneId);
                })
                .map(updatedUser -> ResponseEntity.ok(
                        LocalApiResponse.success(
                                UserResponseModel.fromEntity(updatedUser),
                                "Timezone updated successfully",
                                200
                        )
                ));
    }

    @PreAuthorize("hasRole('TRADER')")
    @GetMapping("/users/name")
    public Mono<LocalApiResponse<UserResponseModel>> findByName(String userName) {
        return userService.findByUsername(userName)
                .map(user -> {
                    UserResponseModel responseModel = UserResponseModel.builder()
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .build();
                    return LocalApiResponse.success(responseModel, "User found", 200);
                });
    }
}
