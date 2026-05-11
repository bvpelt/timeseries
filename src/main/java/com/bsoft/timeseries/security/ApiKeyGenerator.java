package com.bsoft.timeseries.security;

import java.util.UUID;

// Pure utility — no Spring, no DB dependency, trivially testable
public final class ApiKeyGenerator {

    private static final String API_KEY_PATTERN =
            "^([0-9a-f]{8})-([0-9a-f]{4})-([0-9a-f]{4})-([0-9a-f]{4})-([0-9a-f]{12})$";

    private ApiKeyGenerator() {
    }

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static boolean matchesPattern(String key) {
        return key != null && key.matches(API_KEY_PATTERN);
    }
}