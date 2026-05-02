package com.bsoft.timeseries.repository;

import com.bsoft.timeseries.entity.AgreementEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface AgreementRepository extends BitemporalRepository<AgreementEntity> {
}