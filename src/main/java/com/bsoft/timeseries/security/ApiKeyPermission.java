package com.bsoft.timeseries.security;

/**
 * Permission levels for API keys.
 * Maps directly to the {@code permission} column in the {@code api_key} table.
 */
public enum ApiKeyPermission {

    /**
     * Full read + write access (GET, POST, PUT, DELETE).
     */
    READ_WRITE,

    /**
     * Read-only access (GET only).
     */
    READ,

    /**
     * No access — all requests are denied (effectively a revoked key).
     */
    NO_ACCESS
}