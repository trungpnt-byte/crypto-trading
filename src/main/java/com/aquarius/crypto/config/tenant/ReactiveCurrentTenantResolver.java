package com.aquarius.crypto.config.tenant;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

@Component
public class ReactiveCurrentTenantResolver {

    public static final String DEFAULT_TENANT = "public"; // fallback tenant
    
    public static String resolveTenantFromContext(ContextView contextView) {
        return contextView.getOrDefault(TenantContext.TENANT_ID_KEY, DEFAULT_TENANT);
    }

    /**
     * Resolve the current tenant from Reactor context.
     * Can be used in repositories or services before executing queries.
     */
    public Mono<String> resolveCurrentTenant() {
        return Mono.deferContextual(ctx -> {
            String tenantId = ctx.getOrDefault(TenantContext.TENANT_ID_KEY, DEFAULT_TENANT);
            return Mono.just(tenantId);
        });
    }
}