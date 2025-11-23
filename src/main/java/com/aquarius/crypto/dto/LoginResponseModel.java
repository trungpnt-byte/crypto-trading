package com.aquarius.crypto.dto;

public record LoginResponseModel(
        String accessToken,
        String tokenType,
        Long expiresIn
) {
}
