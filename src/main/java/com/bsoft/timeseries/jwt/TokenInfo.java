package com.bsoft.timeseries.jwt;

import java.util.Date;
import java.util.Map;

// ─── Value record to carry decoded token data ─────────────────────────────

public record TokenInfo(
        String subject,
        Date issuedAt,
        Date expiration,
        String issuer,
        Map<String, Object> allClaims
) {
    public boolean isExpired() {
        return expiration != null && expiration.before(new Date());
    }

    public long remainingSeconds() {
        if (expiration == null) return -1;
        return Math.max(0, (expiration.getTime() - System.currentTimeMillis()) / 1000);
    }

    @Override
    public String toString() {
        return String.format(
                "TokenInfo{subject='%s', issuedAt=%s, expiration=%s, issuer='%s', expired=%s, remainingSeconds=%d, claims=%s}",
                subject, issuedAt, expiration, issuer, isExpired(), remainingSeconds(), allClaims
        );
    }
}