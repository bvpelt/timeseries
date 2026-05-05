package com.bsoft.timeseries.delegate;

import java.time.OffsetDateTime;

/**
 * Shared utilities for delegate implementations.
 * Converts optional bitemporal query parameters — defaulting to
 * "current time" when the caller does not supply a value, which gives
 * the standard "show me the current state" behaviour.
 */
//final class DelegateSupport {
public class DelegateSupport {

    public DelegateSupport() {
    }

    public static OffsetDateTime resolveTime(OffsetDateTime requested) {
        return requested != null ? requested : OffsetDateTime.now();
    }

    public static OffsetDateTime resolveValidAt(OffsetDateTime validAt) {
        return resolveTime(validAt);
    }

    public static OffsetDateTime resolveTransactionAt(OffsetDateTime transactionAt) {
        return resolveTime(transactionAt);
    }
}