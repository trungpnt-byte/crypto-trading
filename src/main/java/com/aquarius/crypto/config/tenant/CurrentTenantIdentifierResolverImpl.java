package com.aquarius.crypto.config.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CurrentTenantIdentifierResolverImpl implements CurrentTenantIdentifierResolver<String> {
    static final String DEFAULT_TENANT = "default";

    @Override
    public String resolveCurrentTenantIdentifier() {
//        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
//                .filter(Predicate.not(authentication -> authentication instanceof AnonymousAuthenticationToken))
//                .map(Principal::getName)
//                .orElse(DEFAULT_TENANT);
        return TenantContext.getCurrentTenant();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}