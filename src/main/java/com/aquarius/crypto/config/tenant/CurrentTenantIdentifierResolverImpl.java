package com.aquarius.crypto.config.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;
import java.util.function.Predicate;

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