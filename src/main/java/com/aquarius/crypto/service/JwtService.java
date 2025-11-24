package com.aquarius.crypto.service;

import com.aquarius.crypto.common.JwtProperties;
import com.aquarius.crypto.common.UUIDConverter;
import com.aquarius.crypto.common.UUIDHelper;
import com.aquarius.crypto.model.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.MissingClaimException;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtService {

    private final JwtProperties props;
    private final RSAPublicKey publicKey; // Used for verification (parser)
    private final RSAPrivateKey privateKey; // Used for signing
    private final JwtParser parser;

    public JwtService(JwtProperties props, RSAPublicKey publicKey, RSAPrivateKey privateKey) {
        this.props = props;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.parser = Jwts.parserBuilder().setSigningKey(this.publicKey).build();
    }

    public Map<String, String> validateAndExtractClaims(String token) {
        if (isTokenExpired(token)) {
            throw new ExpiredJwtException(null, null, "JWT token is expired.");
        }

        Claims claims = extractAllClaims(token);

        String publicIdStr = claims.get("user_public_id", String.class);
        String tenantId = claims.get("tenant_id", String.class);

        if (publicIdStr == null) {
            throw new MissingClaimException(null, claims, "Required claim 'user_public_id' is missing.");
        }
        UUIDHelper.ensureValid(publicIdStr);
        if (tenantId == null) {
            throw new MissingClaimException(null, claims, "Required claim 'tenant_id' is missing.");
        }

        Map<String, String> extractedClaims = new HashMap<>();
        extractedClaims.put("publicId", publicIdStr);
        extractedClaims.put("tenantId", tenantId);

        return extractedClaims;
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return parser.parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("user_public_id", principal.publicId().toString());
        claims.put("tenant_id", principal.tenantId());
        claims.put("roles", principal.getAuthorities());
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .setClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(props.getAccessTokenTtlSeconds())))
                .signWith(this.privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    public String generateRefreshToken(UserPrincipal principal) {
        Instant now = Instant.now();
        String subject = UUIDConverter.uuidToString(principal.publicId());
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(props.getRefreshTokenTtlSeconds())))
                .signWith(this.privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}

