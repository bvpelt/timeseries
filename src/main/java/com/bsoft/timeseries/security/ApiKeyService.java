package com.bsoft.timeseries.security;

import com.bsoft.timeseries.entity.ApiKeyEntity;
import com.bsoft.timeseries.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository repository;

    /**
     * Looks up a key and returns its permission string,
     * or empty if the key is unknown, inactive, or expired.
     */
    public Optional<String> resolvePermission(String keyValue) {
        return repository.findByKeyValueAndActiveTrue(keyValue)
                .filter(k -> k.getExpiresAt() == null
                        || k.getExpiresAt().isAfter(OffsetDateTime.now()))
                .map(ApiKeyEntity::getPermission);
    }
}