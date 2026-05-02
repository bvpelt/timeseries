package com.bsoft.timeseries.repository;

import com.bsoft.timeseries.entity.PersonEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends BitemporalRepository<PersonEntity> {
    // All required methods are inherited from BitemporalRepository.
    // Domain-specific queries can be added here as the application evolves.
}