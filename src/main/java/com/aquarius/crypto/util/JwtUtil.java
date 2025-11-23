package com.aquarius.crypto.util;

import com.aquarius.crypto.common.JwtProperties;
import com.aquarius.crypto.entities.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Component
public class JwtUtil {

    private final JwtProperties props;
    private final SecretKey key;
    private final JwtParser parser;

    public JwtUtil(JwtProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
        this.parser = Jwts.parserBuilder().setSigningKey(key).build();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
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

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return generateAccessToken(claims, userDetails.getUsername());
    }

    private String generateAccessToken(Map<String, Object> claims, String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(props.getAccessTokenTtlSeconds())))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .claim("roles", principal.getAuthorities()) // custom claims
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(props.getAccessTokenTtlSeconds())))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(UserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(props.getRefreshTokenTtlSeconds())))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseClaims(String token) throws JwtException {
        return parser.parseClaimsJws(token);
    }

    public String extractUserName(String token) {
        // extract the username from jwt token
        return extractClaim(token, Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String userName = extractUserName(token);
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }


    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }

    public String getUsername(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Claims c = parseClaims(token).getBody();
        Object roles = c.get("roles");
        if (roles instanceof List) {
            return (List<String>) roles;
        }
        return Collections.emptyList();
    }
}

