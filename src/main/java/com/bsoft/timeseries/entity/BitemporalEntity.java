package com.bsoft.timeseries.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Abstract base that every bitemporal table entity extends.
 *
 * <p>Two independent time axes (Richard Snodgrass model):
 * <ul>
 *   <li><b>Valid Time</b>  – when the fact is true in the real world
 *       ({@code validFrom} / {@code validTo}). Controlled by the caller.</li>
 *   <li><b>Transaction Time</b> – when the record was written to the database
 *       ({@code transactionFrom} / {@code transactionTo}). Controlled by the
 *       system; never modified after insert.</li>
 * </ul>
 *
 * <p>Sentinel value for an "open" / "still current" end:
 * {@code 9999-12-31T23:59:59Z} — stored as a real timestamp so ordinary
 * range operators work without special-casing NULL.
 */
@MappedSuperclass
@Getter
@Setter
public abstract class BitemporalEntity {

    public static final OffsetDateTime INFINITY =
            OffsetDateTime.parse("9999-12-31T23:59:59Z");

    // -----------------------------------------------------------------
    // Surrogate PK (internal, never exposed in the API)
    // -----------------------------------------------------------------
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    /**
     * Stable business identifier. The same UUID is shared by all
     * transaction-time versions of the same logical entity.
     */
    @Column(name = "uid", nullable = false, updatable = false)
    private UUID uid;

    // -----------------------------------------------------------------
    // Valid Time  (caller-controlled)
    // -----------------------------------------------------------------
    @Column(name = "valid_from", nullable = false)
    private OffsetDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private OffsetDateTime validTo;

    // -----------------------------------------------------------------
    // Transaction Time  (system-controlled, set on insert, never changed)
    // -----------------------------------------------------------------
    @Column(name = "transaction_from", nullable = false, updatable = false)
    private OffsetDateTime transactionFrom;

    @Column(name = "transaction_to", nullable = false)
    private OffsetDateTime transactionTo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // -----------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------

    /**
     * True when this row is the "current" transaction-time version.
     */
    public boolean isCurrentTransaction() {
        return INFINITY.equals(transactionTo);
    }

    /**
     * True when this row is valid at the given point in valid time.
     */
    public boolean isValidAt(OffsetDateTime point) {
        return !validFrom.isAfter(point) && validTo.isAfter(point);
    }

    /**
     * Closes the transaction-time window — marks this version as superseded.
     */
    public void closeTransaction(OffsetDateTime at) {
        this.transactionTo = at;
    }

    /**
     * Closes the valid-time window — ends validity in the real world.
     */
    public void closeValid(OffsetDateTime at) {
        this.validTo = at;
    }

    @PrePersist
    protected void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (uid == null) uid = UUID.randomUUID();
        if (validFrom == null) validFrom = now;
        if (validTo == null) validTo = INFINITY;
        if (transactionFrom == null) transactionFrom = now;
        if (transactionTo == null) transactionTo = INFINITY;
        if (createdAt == null) createdAt = now;
    }
}