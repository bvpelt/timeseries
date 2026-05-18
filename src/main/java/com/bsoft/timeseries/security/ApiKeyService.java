package com.bsoft.timeseries.security;

import com.bsoft.timeseries.entity.ApiKeyEntity;
import com.bsoft.timeseries.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@RequiredArgsConstructor
@Service
public class ApiKeyService {

    private static final String API_KEY_PATTERN =
            "^([0-9a-f]{8})-([0-9a-f]{4})-([0-9a-f]{4})-([0-9a-f]{4})-([0-9a-f]{12})$";

    private final ApiKeyRepository repository;

    public boolean isValidApiKey(String apiKey) {
        if (!apiKey.matches(API_KEY_PATTERN)) {
            return false;
        }
        Optional<ApiKeyEntity> apiKeyOptional =
                repository.findByKeyValueAndActiveTrue(apiKey);
        boolean result = apiKeyOptional.isPresent();
        log.trace("isValidApiKey checking key: {} value: {}", apiKey, result);
        return result;
    }

    public ApiKeyEntity generateApiKey() {
        String randomKey = UUID.randomUUID().toString();
        ApiKeyEntity apiKey = new ApiKeyEntity();
        apiKey.setKeyValue(randomKey);
        return apiKey;
    }

    public Optional<String> resolveOwner(String keyValue) {
        return repository.findByKeyValueAndActiveTrue(keyValue)
                .filter(k -> k.getExpiresAt() == null
                        || k.getExpiresAt().isAfter(OffsetDateTime.now()))
                .map(ApiKeyEntity::getOwner);   // returns owner name for logging
    }
}