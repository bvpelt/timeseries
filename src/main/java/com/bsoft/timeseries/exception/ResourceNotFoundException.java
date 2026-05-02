package com.bsoft.timeseries.exception;

import java.util.UUID;

/**
 * Thrown when an entity cannot be found at the requested bitemporal coordinates
 * (valid time × transaction time).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resourceType, UUID uid) {
        super("No %s found with uid '%s' at the requested bitemporal point."
                .formatted(resourceType, uid));
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}