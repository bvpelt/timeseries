package com.bsoft.timeseries.mapper;

import com.bsoft.timeseries.entity.BitemporalEntity;
import com.bsoft.timeseries.entity.PersonAgreementEntity;
import com.bsoft.timeseries.model.PersonAgreement;
import org.mapstruct.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = AgreementMapper.class)
public interface PersonAgreementMapper {

    @Mapping(target = "uid",             source = "uid")
    @Mapping(target = "personUid",       source = "personUid")
    @Mapping(target = "agreementUid",    source = "agreementUid")
    @Mapping(target = "agreement",       source = "agreement")
    @Mapping(target = "validFrom",       source = "validFrom")
    @Mapping(target = "validTo",         expression = "java(nullIfInfinity(entity.getValidTo()))")
    @Mapping(target = "transactionFrom", source = "transactionFrom")
    @Mapping(target = "transactionTo",   expression = "java(nullIfInfinity(entity.getTransactionTo()))")
    PersonAgreement toDto(PersonAgreementEntity entity);

    List<PersonAgreement> toDtoList(List<PersonAgreementEntity> entities);

    default OffsetDateTime nullIfInfinity(OffsetDateTime value) {
        return BitemporalEntity.INFINITY.equals(value) ? null : value;
    }

    default String map(UUID value) {
        return value == null ? null : value.toString();
    }
}