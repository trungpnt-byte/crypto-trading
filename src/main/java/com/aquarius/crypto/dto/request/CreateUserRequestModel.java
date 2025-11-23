package com.aquarius.crypto.dto.request;

public record CreateUserRequestModel(
        String username,
        String password,
        String email,
        String role
) {
}
