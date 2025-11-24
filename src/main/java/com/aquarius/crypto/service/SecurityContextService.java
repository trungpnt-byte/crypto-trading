package com.aquarius.crypto.service;

import com.aquarius.crypto.common.UUIDConverter;
import com.aquarius.crypto.model.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class SecurityContextService {

    private final UserMappingService userMappingService;

    public SecurityContextService(UserMappingService userMappingService) {
        this.userMappingService = userMappingService;
    }

    public Mono<Long> getInternalUserId() {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .switchIfEmpty(Mono.error(() -> new UsernameNotFoundException("Authentication context not found.")))
                .mapNotNull(Authentication::getPrincipal)
                .cast(String.class)
                .flatMap(uuidStr -> {
                    try {
                        UUID publicId = UUIDConverter.stringToUuid(uuidStr);
                        return userMappingService.findByPublicId(publicId);
                    } catch (IllegalArgumentException e) {
                        return Mono.error(new IllegalStateException("Invalid UUID format in Principal.", e));
                    }
                })
                .map(UserPrincipal::internalId)

                .switchIfEmpty(Mono.error(() -> new UsernameNotFoundException("User identity could not be resolved.")));
    }
}