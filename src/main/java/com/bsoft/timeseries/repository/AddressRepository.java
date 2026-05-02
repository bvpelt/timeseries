package com.bsoft.timeseries.repository;

import com.bsoft.timeseries.entity.AddressEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends BitemporalRepository<AddressEntity> {
}