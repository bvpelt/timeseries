package com.bsoft.timeseries.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "agreement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgreementEntity extends BitemporalEntity {

    @Column(name = "title", nullable = false, length = 200)
    private String title;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 10)
    private State state;

    public enum State {NEW, CHANGED, READY}
}