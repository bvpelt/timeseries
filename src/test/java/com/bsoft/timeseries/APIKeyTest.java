package com.bsoft.timeseries;

import com.bsoft.timeseries.security.ApiKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


@Slf4j
public class APIKeyTest {

    @Test
    @DisplayName("Generated keys match UUID pattern")
    public void testGeneratedApiKeysMatchingPattern() {
        for (int i = 0; i < 15; i++) {
            String key = ApiKeyGenerator.generate();
            log.info("Generated api key: {}", key);
            assertTrue(ApiKeyGenerator.matchesPattern(key),
                    "Key should match UUID pattern: " + key);
        }
    }
}
