package com.bsoft.timeseries.repository;

import com.bsoft.timeseries.entity.PersonAddressEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonAddressRepository
        extends BitemporalRepository<PersonAddressEntity> {

    /**
     * Returns all address links for a person at the given bitemporal point.
     */
    @Query("""
            SELECT pa FROM PersonAddressEntity pa
            WHERE pa.personUid        = :personUid
              AND pa.validFrom       <= :validAt
              AND pa.validTo          > :validAt
              AND pa.transactionFrom <= :transactionAt
              AND pa.transactionTo    > :transactionAt
            ORDER BY pa.addressType, pa.validFrom
            """)
    List<PersonAddressEntity> findByPersonAtPoint(
            @Param("personUid") UUID personUid,
            @Param("validAt") OffsetDateTime validAt,
            @Param("transactionAt") OffsetDateTime transactionAt);

    /**
     * Finds a specific person-address link at the bitemporal point.
     * Used to check existence before creating a duplicate.
     */
    @Query("""
            SELECT pa FROM PersonAddressEntity pa
            WHERE pa.personUid        = :personUid
              AND pa.addressUid       = :addressUid
              AND pa.validFrom       <= :validAt
              AND pa.validTo          > :validAt
              AND pa.transactionFrom <= :transactionAt
              AND pa.transactionTo    > :transactionAt
            """)
    Optional<PersonAddressEntity> findLinkAtPoint(
            @Param("personUid") UUID personUid,
            @Param("addressUid") UUID addressUid,
            @Param("validAt") OffsetDateTime validAt,
            @Param("transactionAt") OffsetDateTime transactionAt);

    /**
     * Counts active HOME address links for a person in current transaction time.
     * Used to enforce the "at most one current HOME" business rule.
     */
    @Query("""
            SELECT COUNT(pa) FROM PersonAddressEntity pa
            WHERE pa.personUid   = :personUid
              AND pa.addressType = com.bsoft.timeseries.entity.PersonAddressEntity$AddressType.HOME
              AND pa.transactionTo = :infinity
              AND pa.validFrom  <= :now
              AND pa.validTo     > :now
            """)
    long countCurrentHomeAddresses(
            @Param("personUid") UUID personUid,
            @Param("now") OffsetDateTime now,
            @Param("infinity") OffsetDateTime infinity);

    /**
     * Closes current transaction-time versions of a specific person-address link.
     */
    @Query("""
            UPDATE PersonAddressEntity pa
            SET    pa.transactionTo = :closedAt
            WHERE  pa.personUid     = :personUid
              AND  pa.addressUid    = :addressUid
              AND  pa.transactionTo = :infinity
            """)
    @org.springframework.data.jpa.repository.Modifying
    int closeLinkTransaction(
            @Param("personUid") UUID personUid,
            @Param("addressUid") UUID addressUid,
            @Param("closedAt") OffsetDateTime closedAt,
            @Param("infinity") OffsetDateTime infinity);
}