package com.aquarius.crypto.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Builder
@Setter
@Data
public class AuthResponseModel {
    private String accessToken;
    private String refreshToken;
    private String publicId;
}
