package com.aquarius.crypto.dto;

import lombok.Builder;

@Builder
public record UserResponseModel(
        String username,
        String email) {
}
