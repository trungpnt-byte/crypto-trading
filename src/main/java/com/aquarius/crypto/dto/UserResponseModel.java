package com.aquarius.crypto.dto;

import com.aquarius.crypto.model.User;
import lombok.Builder;

@Builder
public record UserResponseModel(
        String username,
        String email) {
    public static UserResponseModel fromEntity(User user) {
        return UserResponseModel.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}
