package com.aquarius.crypto.filter;


import com.aquarius.crypto.common.ExtractionHelper;
import com.aquarius.crypto.common.UUIDConverter;
import com.aquarius.crypto.config.tenant.TenantMappingService;
import com.aquarius.crypto.exception.UserAuthenticationException;
import com.aquarius.crypto.service.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.aquarius.crypto.constants.ConstStrings.BEARER;

@Component
@Slf4j
public class JwtAuthenticationWebFilter implements WebFilter {

    private final JwtService jwtService;
    private final TenantMappingService tenantMappingService;

    public JwtAuthenticationWebFilter(JwtService jwtService, TenantMappingService tenantMappingService) {
        this.jwtService = jwtService;
        this.tenantMappingService = tenantMappingService;
    }

    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER)) {
            return chain.filter(exchange);
        }

        final String jwtToken = ExtractionHelper.extractTokenValue(authHeader);

        return Mono.fromCallable(() -> jwtService.validateAndExtractClaims(jwtToken))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(claims -> {
                    String publicIdStr = claims.get("publicId");
                    String tenantClaim = claims.get("tenantId");
                    String schema = tenantMappingService.mapJwtTenantToSchema(tenantClaim)
                            .orElseThrow(() -> new UserAuthenticationException("Invalid tenant claim: " + tenantClaim));

                    Authentication auth = createAuthenticationToken(Objects.requireNonNull(UUIDConverter.stringToUuid(publicIdStr)));

                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth))
                            .contextWrite(ctx -> ctx.put("TENANT_ID_KEY", schema));
                })
                .onErrorResume(e -> {
                    log.warn("Authentication failure (JWT/Tenant): {}", e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }

    private Authentication createAuthenticationToken(UUID publicId) {
        return new UsernamePasswordAuthenticationToken(
                publicId.toString(),
                null,
                List.of(new SimpleGrantedAuthority("TRADER"))
        );
    }
}