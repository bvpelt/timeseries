package com.bsoft.timeseries.repository;

import com.bsoft.timeseries.entity.PersonAgreementEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonAgreementRepository
        extends BitemporalRepository<PersonAgreementEntity> {

    /**
     * All agreement links for a person at the given bitemporal point.
     */
    @Query("""
            SELECT pa FROM PersonAgreementEntity pa
            WHERE pa.personUid        = :personUid
              AND pa.validFrom       <= :validAt
              AND pa.validTo          > :validAt
              AND pa.transactionFrom <= :transactionAt
              AND pa.transactionTo    > :transactionAt
            ORDER BY pa.validFrom
            """)
    List<PersonAgreementEntity> findByPersonAtPoint(
            @Param("personUid") UUID personUid,
            @Param("validAt") OffsetDateTime validAt,
            @Param("transactionAt") OffsetDateTime transactionAt);

    /**
     * Checks whether a specific link already exists at the given point in time.
     */
    @Query("""
            SELECT pa FROM PersonAgreementEntity pa
            WHERE pa.personUid        = :personUid
              AND pa.agreementUid     = :agreementUid
              AND pa.validFrom       <= :validAt
              AND pa.validTo          > :validAt
              AND pa.transactionFrom <= :transactionAt
              AND pa.transactionTo    > :transactionAt
            """)
    Optional<PersonAgreementEntity> findLinkAtPoint(
            @Param("personUid") UUID personUid,
            @Param("agreementUid") UUID agreementUid,
            @Param("validAt") OffsetDateTime validAt,
            @Param("transactionAt") OffsetDateTime transactionAt);

    /**
     * Closes the current transaction-time version of a specific person-agreement link.
     */
    @Modifying
    @Query("""
            UPDATE PersonAgreementEntity pa
            SET    pa.transactionTo  = :closedAt
            WHERE  pa.personUid      = :personUid
              AND  pa.agreementUid   = :agreementUid
              AND  pa.transactionTo  = :infinity
            """)
    int closeLinkTransaction(
            @Param("personUid") UUID personUid,
            @Param("agreementUid") UUID agreementUid,
            @Param("closedAt") OffsetDateTime closedAt,
            @Param("infinity") OffsetDateTime infinity);
}