package com.aquarius.crypto.config.tenant;

import com.aquarius.crypto.common.ExtractionHelper;
import com.aquarius.crypto.config.security.SecurityDomain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class TenantWebFilter implements WebFilter {

    private final SecurityDomain securityDomain;

    public TenantWebFilter(SecurityDomain securityDomain) {
        this.securityDomain = securityDomain;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        Mono<String> tenantIdMono = getTenantIdFromRequest(exchange);

        return tenantIdMono
                .defaultIfEmpty(TenantContext.DEFAULT_TENANT_ID)
                .flatMap(tenantId ->
                        chain.filter(exchange)
                                .contextWrite(ctx -> ctx.put(TenantContext.TENANT_ID_KEY, tenantId))
                );
    }

    private Mono<String> getTenantIdFromRequest(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = ExtractionHelper.extractTokenValue(authHeader);
            return securityDomain.getTenantIdFromJwt(jwt);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (path.split("/").length > 2) {
            String possibleTenant = path.split("/")[2];
            return Mono.just(possibleTenant);
        }

        return Mono.empty();
    }
}