package com.bsoft.timeseries.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Bitemporal N:M relation between a person and an agreement.
 * Many persons can be party to the same agreement, and a person can be
 * party to many agreements simultaneously.
 */
@Entity
@Table(name = "person_agreement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonAgreementEntity extends BitemporalEntity {

    @Column(name = "person_uid", nullable = false)
    private UUID personUid;

    @Column(name = "agreement_uid", nullable = false)
    private UUID agreementUid;

    // Read-only join — not cascaded
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agreement_uid", referencedColumnName = "uid",
            insertable = false, updatable = false)
    private AgreementEntity agreement;
}