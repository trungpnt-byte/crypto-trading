package com.aquarius.crypto.security;

import com.aquarius.crypto.common.JwtProperties;
import com.aquarius.crypto.model.UserPrincipal;
import com.aquarius.crypto.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MissingClaimException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private JwtProperties props;
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    @BeforeEach
    void setUp() throws Exception {
        // Generate RSA key pair for signing/verifying
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        publicKey = (RSAPublicKey) keyPair.getPublic();
        privateKey = (RSAPrivateKey) keyPair.getPrivate();

        // Mock properties
        props = Mockito.mock(JwtProperties.class);
        Mockito.when(props.getAccessTokenTtlSeconds()).thenReturn(3600L);  // 1 hour
        Mockito.when(props.getRefreshTokenTtlSeconds()).thenReturn(604800L); // 1 week

        jwtService = new JwtService(props, publicKey, privateKey);
    }

    @Test
    void testGenerateAndValidateAccessToken() {
        UserPrincipal principal = new UserPrincipal(
                1L,
                UUID.randomUUID(),
                "user1",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                "tenant-1"
        );

        String token = jwtService.generateAccessToken(principal);
        assertNotNull(token);

        Map<String, String> claims = jwtService.validateAndExtractClaims(token);
        assertEquals(principal.publicId().toString(), claims.get("publicId"));
        assertEquals(principal.tenantId(), claims.get("tenantId"));
    }

    @Test
    void testGenerateAndValidateRefreshToken() {
        UserPrincipal principal = new UserPrincipal(
                1L,
                UUID.randomUUID(),
                "user1",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")),
                "tenant-1"
        );

        String token = jwtService.generateRefreshToken(principal);
        assertNotNull(token);

        // Refresh token's subject should equal the publicId as string
        String subject = jwtService.extractClaim(token, Claims::getSubject);
        assertEquals(principal.getName().toString(), subject);
    }

    @Test
    void testExpiredTokenThrowsException() {
        Instant now = Instant.now();

        String expiredToken = Jwts.builder()
                .setSubject("user1")
                .setExpiration(Date.from(now.minusSeconds(10))) // already expired
                .signWith(privateKey, io.jsonwebtoken.SignatureAlgorithm.RS256)
                .compact();

        assertThrows(ExpiredJwtException.class, () -> jwtService.validateAndExtractClaims(expiredToken));
    }

    @Test
    void testMissingClaimsThrowsException() {
        Instant now = Instant.now();

        // Token missing required claims
        String token = Jwts.builder()
                .setSubject("user1")
                .setExpiration(Date.from(now.plusSeconds(3600)))
                .signWith(privateKey, io.jsonwebtoken.SignatureAlgorithm.RS256)
                .compact();

        MissingClaimException exception = assertThrows(MissingClaimException.class,
                () -> jwtService.validateAndExtractClaims(token));

        // Should mention missing claims in message
        String msg = exception.getMessage();
        assertTrue(msg.contains("user_public_id") || msg.contains("tenant_id"));
    }
}