package com.aquarius.crypto.common;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    // JWT signing
    private String secret;

    // Access token TTL in seconds
    private Long accessTokenTtlSeconds;

    // Refresh token TTL in seconds
    private Long refreshTokenTtlSeconds;

    // Cookie options
    private String cookieDomain;
    private String cookiePath;
}
