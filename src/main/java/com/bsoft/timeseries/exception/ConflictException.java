package com.bsoft.timeseries.exception;

/**
 * Thrown when a create / link operation violates a uniqueness or business constraint —
 * for example, attempting to assign a second HOME address to a person within the same
 * valid-time interval.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}