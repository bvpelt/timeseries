package com.bsoft.timeseries.delegate;

import java.time.OffsetDateTime;

/**
 * Shared utilities for delegate implementations.
 * Converts optional bitemporal query parameters — defaulting to
 * "current time" when the caller does not supply a value, which gives
 * the standard "show me the current state" behaviour.
 */
final class DelegateSupport {

    private DelegateSupport() {}

    static OffsetDateTime resolveTime(OffsetDateTime requested) {
        return requested != null ? requested : OffsetDateTime.now();
    }

    static OffsetDateTime resolveValidAt(OffsetDateTime validAt) {
        return resolveTime(validAt);
    }

    static OffsetDateTime resolveTransactionAt(OffsetDateTime transactionAt) {
        return resolveTime(transactionAt);
    }
}