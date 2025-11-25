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

    public Mono<UserPrincipal> getCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .mapNotNull(SecurityContext::getAuthentication)
                .switchIfEmpty(Mono.error(() ->
                        new UsernameNotFoundException("Authentication context not found.")))
                .mapNotNull(Authentication::getPrincipal)
                .cast(String.class)
                .flatMap(this::findUserByPublicIdString);
    }

    public Mono<UUID> getCurrentPublicId() {
        return getCurrentUser()
                .map(UserPrincipal::publicId);
    }

    public Mono<Long> getInternalUserId() {
        return getCurrentUser()
                .map(UserPrincipal::internalId);
    }

    private Mono<UserPrincipal> findUserByPublicIdString(String uuidStr) {
        try {
            UUID publicId = UUIDConverter.stringToUuid(uuidStr);
            return findUserByPublicId(publicId);
        } catch (IllegalArgumentException e) {
            return Mono.error(new IllegalStateException("Invalid UUID format in Principal.", e));
        }
    }

    private Mono<UserPrincipal> findUserByPublicId(UUID publicId) {
        return userMappingService.findByPublicId(publicId)
                .switchIfEmpty(Mono.error(() ->
                        new UsernameNotFoundException("User not found for publicId: " + publicId)));
    }
}