package com.bsoft.timeseries.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * Spring Security {@link org.springframework.security.core.Authentication} token
 * representing an authenticated API key request.
 *
 * <p>The token carries the permission level as a {@link SimpleGrantedAuthority},
 * allowing Spring Security {@code @PreAuthorize} expressions to check authorities
 * like {@code hasAuthority('READ_WRITE')} or {@code hasAuthority('READ')}.</p>
 */
public class ApiKeyAuthentication extends AbstractAuthenticationToken {

    private final String rawApiKey;
    private final ApiKeyPermission permission;

    public ApiKeyAuthentication(String rawApiKey, ApiKeyPermission permission) {
        super(List.of(new SimpleGrantedAuthority(permission.name())));
        this.rawApiKey = rawApiKey;
        this.permission = permission;
        // Mark as fully authenticated — the filter has already validated the key.
        super.setAuthenticated(true);
    }

    /**
     * Returns the raw API key value (treated as credentials).
     */
    @Override
    public Object getCredentials() {
        return rawApiKey;
    }

    /**
     * Returns the raw API key value (used as principal identifier).
     */
    @Override
    public Object getPrincipal() {
        return rawApiKey;
    }

    public ApiKeyPermission getPermission() {
        return permission;
    }
}