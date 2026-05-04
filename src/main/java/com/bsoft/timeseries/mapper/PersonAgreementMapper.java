package com.bsoft.timeseries.mapper;

import com.bsoft.timeseries.entity.PersonAgreementEntity;
import com.bsoft.timeseries.model.PersonAgreement;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

/**
 * Same pattern as PersonAddressMapper — see that class for the full explanation.
 * No local OffsetDateTime helper; the @Named("nullIfInfinity") method from
 * AgreementMapper is referenced explicitly via qualifiedByName on validTo and
 * transactionTo only.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = AgreementMapper.class)
public interface PersonAgreementMapper {

    @Mapping(target = "uid",             source = "uid")
    @Mapping(target = "personUid",       source = "personUid")
    @Mapping(target = "agreementUid",    source = "agreementUid")
    @Mapping(target = "agreement",       source = "agreement")
    @Mapping(target = "validFrom",       source = "validFrom")
    @Mapping(target = "validTo",         source = "validTo",         qualifiedByName = "nullIfInfinity")
    @Mapping(target = "transactionFrom", source = "transactionFrom")
    @Mapping(target = "transactionTo",   source = "transactionTo",   qualifiedByName = "nullIfInfinity")
    PersonAgreement toDto(PersonAgreementEntity entity);

    List<PersonAgreement> toDtoList(List<PersonAgreementEntity> entities);

    default String mapUuid(UUID value) {
        return value == null ? null : value.toString();
    }
}