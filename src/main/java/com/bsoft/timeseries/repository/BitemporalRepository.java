package com.bsoft.timeseries.repository;

import com.bsoft.timeseries.entity.BitemporalEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Base repository fragment providing generic bitemporal query methods.
 *
 * <p>All queries follow Snodgrass convention:
 * <pre>
 *   valid_from  <= queryPoint < valid_to
 *   transaction_from <= queryPoint < transaction_to
 * </pre>
 *
 * @param <T> the entity type (must extend {@link BitemporalEntity})
 */
@NoRepositoryBean
public interface BitemporalRepository<T extends BitemporalEntity>
        extends JpaRepository<T, Long> {

    // ------------------------------------------------------------------
    // Point-in-time single-entity lookups
    // ------------------------------------------------------------------

    /**
     * Finds the single version of an entity at a specific point in the
     * bitemporal plane (valid time AND transaction time).
     */
    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE e.uid = :uid
              AND e.validFrom      <= :validAt
              AND e.validTo         > :validAt
              AND e.transactionFrom <= :transactionAt
              AND e.transactionTo    > :transactionAt
            """)
    Optional<T> findAtPoint(
            @Param("uid") UUID uid,
            @Param("validAt") OffsetDateTime validAt,
            @Param("transactionAt") OffsetDateTime transactionAt);

    /**
     * Convenience: current valid + current transaction version.
     */
    default Optional<T> findCurrent(UUID uid, OffsetDateTime now) {
        return findAtPoint(uid, now, now);
    }

    // ------------------------------------------------------------------
    // Full history
    // ------------------------------------------------------------------

    /**
     * All rows for a given business uid, ordered oldest-first across
     * both time axes.
     */
    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE e.uid = :uid
            ORDER BY e.transactionFrom ASC, e.validFrom ASC
            """)
    List<T> findAllVersions(@Param("uid") UUID uid);

    // ------------------------------------------------------------------
    // Paginated current-state listing
    // ------------------------------------------------------------------

    /**
     * All entities valid and transaction-current at the given point in time,
     * with pagination.
     */
    @Query("""
            SELECT e FROM #{#entityName} e
            WHERE e.validFrom      <= :validAt
              AND e.validTo         > :validAt
              AND e.transactionFrom <= :transactionAt
              AND e.transactionTo    > :transactionAt
            """)
    Page<T> findAllAtPoint(
            @Param("validAt") OffsetDateTime validAt,
            @Param("transactionAt") OffsetDateTime transactionAt,
            Pageable pageable);

    // ------------------------------------------------------------------
    // Transaction-time closing (used during update / terminate)
    // ------------------------------------------------------------------

    /**
     * Closes the transaction-time window of the current version so a new
     * version can be inserted.  Only rows where transactionTo = INFINITY
     * are affected, preventing double-closing.
     */
    @Modifying
    @Query("""
            UPDATE #{#entityName} e
            SET    e.transactionTo = :closedAt
            WHERE  e.uid           = :uid
              AND  e.transactionTo  = :infinity
            """)
    int closeCurrentTransactionVersion(
            @Param("uid") UUID uid,
            @Param("closedAt") OffsetDateTime closedAt,
            @Param("infinity") OffsetDateTime infinity);

    /**
     * Closes the valid-time window of the current (transaction-open) version.
     * Used when the business fact ends but is not corrected.
     */
    @Modifying
    @Query("""
            UPDATE #{#entityName} e
            SET    e.validTo = :validTo
            WHERE  e.uid          = :uid
              AND  e.transactionTo = :infinity
              AND  e.validTo       = :infinity
            """)
    int closeCurrentValidVersion(
            @Param("uid") UUID uid,
            @Param("validTo") OffsetDateTime validTo,
            @Param("infinity") OffsetDateTime infinity);
}