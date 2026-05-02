package com.bsoft.timeseries.repository;

import com.bsoft.timeseries.entity.ApiKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {

    /**
     * Looks up an API key by its SHA-256 hash.
     * Only active, non-expired keys are considered.
     */
    @Query("""
            SELECT k FROM ApiKeyEntity k
            WHERE k.keyHash = :keyHash
              AND k.active  = true
              AND (k.expiresAt IS NULL OR k.expiresAt > CURRENT_TIMESTAMP)
            """)
    Optional<ApiKeyEntity> findActiveByKeyHash(@Param("keyHash") String keyHash);
}