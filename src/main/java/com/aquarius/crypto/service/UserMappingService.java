package com.aquarius.crypto.service;


import com.aquarius.crypto.model.UserPrincipal;
import com.aquarius.crypto.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserMappingService {

    private final UserRepository userRepository;

    /**
     * Finds the full UserPrincipal object based on the public UUID provided in the JWT.
     * This method is used by the JwtContextResolverFilter to map the external ID
     * to the internal ID and necessary security claims (Tenant ID).
     * * @param publicId The UUID from the JWT subject claim.
     *
     * @return Mono<UserPrincipal> with all fields populated.
     */
    @Cacheable("userMapping")
    public Mono<UserPrincipal> findByPublicId(UUID publicId) {
        return userRepository.findByPublicId(publicId)
                .switchIfEmpty(Mono.error(() -> new IllegalArgumentException("User Public ID not found.")))
                .map(user -> {
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(
                            new SimpleGrantedAuthority(user.getSimpleRole().toUpperCase())
                    );
                    String tenantId = user.getTenantId();
                    return new UserPrincipal(
                            user.getId(),
                            user.getPublicId(),
                            user.getEmail(),
                            null,
                            authorities,
                            tenantId
                    );
                });
    }
}