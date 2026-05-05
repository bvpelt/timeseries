package com.bsoft.timeseries.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Bitemporal N:M relation between a person and an address.
 * Both HOME and WORK address types are stored here; business logic ensures
 * a person has at most one current HOME address in valid time.
 */
@Entity
@Table(name = "person_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonAddressEntity extends BitemporalEntity {

    @Column(name = "person_uid", nullable = false)
    private UUID personUid;
    @Column(name = "address_uid", nullable = false)
    private UUID addressUid;
    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 4)
    private AddressType addressType;
    // Eagerly joined for response hydration — read-only, not cascaded
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_uid", referencedColumnName = "uid",
            insertable = false, updatable = false)
    private AddressEntity address;

    public enum AddressType {HOME, WORK}
}