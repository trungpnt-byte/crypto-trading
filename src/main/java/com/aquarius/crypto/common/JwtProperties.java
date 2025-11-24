package com.aquarius.crypto.common;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    @Value("${jwt.secret:}")
    private String secret;

    private Long accessTokenTtlSeconds;

    private Long refreshTokenTtlSeconds;

    private String cookieDomain;
    private String cookiePath;

}
