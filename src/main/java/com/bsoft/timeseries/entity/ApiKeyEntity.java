package com.bsoft.timeseries.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Persisted API key record. The raw key is <em>never</em> stored —
 * only the SHA-256 hex hash is kept so a database breach does not
 * expose usable secrets.
 */
@Entity
@Table(name = "api_key")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uid", nullable = false, updatable = false)
    @Builder.Default
    private UUID uid = UUID.randomUUID();

    /**
     * SHA-256 hex digest of the raw API key.
     */
    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;

    @Column(name = "description")
    private String description;

    @Column(name = "permission", nullable = false, length = 12)
    @Enumerated(EnumType.STRING)
    private com.bsoft.timeseries.security.ApiKeyPermission permission;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    private OffsetDateTime createdAt;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    /**
     * Returns true when the key is usable: active and not expired.
     */
    public boolean isValid() {
        return active && (expiresAt == null || expiresAt.isAfter(OffsetDateTime.now()));
    }
}