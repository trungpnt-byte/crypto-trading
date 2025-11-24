package com.aquarius.crypto.model;

import java.security.Principal;
import java.util.UUID;


import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * The core security and identity object,
 * extracted from the database during login and stored in the JWT/SecurityContext.
 */
@Builder
public record UserPrincipal(
        // Internal ID (Database FK joins)
        Long internalId,

        // External Secure ID (JWT Subject claim, API Public ID)
        UUID publicId,

        // UserDetails lookup by username/email
        String username,

        // authentication manager during login
        String password,

        // for Spring Security authorization checks
        Collection<? extends GrantedAuthority> authorities,

        String tenantId

) implements UserDetails, Principal {

    @Override
    public String getName() {
        return publicId.toString();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
