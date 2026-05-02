package com.bsoft.timeseries.security;

import com.bsoft.timeseries.entity.ApiKeyEntity;
import com.bsoft.timeseries.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Resolves a raw API key string to its {@link ApiKeyPermission}.
 *
 * <p>The raw key is <em>never</em> stored. We hash it with SHA-256 and
 * compare against the stored digest, so a database breach exposes no usable secrets.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    /**
     * Looks up the permission level for a raw API key.
     *
     * @param rawKey the value from the {@code X-API-Key} request header
     * @return an {@link Optional} containing the permission, or empty if the key
     *         is unknown, inactive, or expired
     */
    @Transactional(readOnly = true)
    public Optional<ApiKeyPermission> resolve(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            return Optional.empty();
        }

        String hash = sha256Hex(rawKey);
        Optional<ApiKeyEntity> entity = apiKeyRepository.findActiveByKeyHash(hash);

        if (entity.isEmpty()) {
            log.debug("API key not found or inactive (hash prefix: {}...)",
                    hash.substring(0, 8));
            return Optional.empty();
        }

        ApiKeyPermission permission = entity.get().getPermission();
        log.debug("API key resolved — permission: {}", permission);
        return Optional.of(permission);
    }

    // ---------------------------------------------------------------

    /**
     * Computes the lowercase hex SHA-256 digest of the input string (UTF-8).
     * Package-private for unit testing.
     */
    static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is mandated by the Java spec — this cannot happen in practice.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}