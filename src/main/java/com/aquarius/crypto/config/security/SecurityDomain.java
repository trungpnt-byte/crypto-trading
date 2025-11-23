package com.aquarius.crypto.config.security;

import com.aquarius.crypto.config.tenant.TenantContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.concurrent.Callable;

@Component
public class SecurityDomain {

    public Mono<String> getTenantIdFromJwt(String jwtToken) {

        Callable<String> blockingTask = () -> {
            if (jwtToken.length() > 10) {
                return TenantContext.TRADER_TENANT_ID;
            } else {
                return TenantContext.DEFAULT_TENANT_ID;
            }
        };
        return Mono.fromCallable(blockingTask)
                .onErrorResume(e -> {
                    System.err.println("JWT processing error: " + e.getMessage());
                    return Mono.empty();
                });
    }
}