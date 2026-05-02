package com.bsoft.timeseries.mapper;

import com.bsoft.timeseries.entity.BitemporalEntity;
import com.bsoft.timeseries.entity.PersonAddressEntity;
import com.bsoft.timeseries.model.AddressType;
import com.bsoft.timeseries.model.PersonAddress;
import org.mapstruct.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = AddressMapper.class)
public interface PersonAddressMapper {

    @Mapping(target = "uid",          source = "uid")
    @Mapping(target = "personUid",    source = "personUid")
    @Mapping(target = "addressUid",   source = "addressUid")
    @Mapping(target = "addressType",  expression = "java(toApiType(entity.getAddressType()))")
    @Mapping(target = "address",      source = "address")
    @Mapping(target = "validFrom",    source = "validFrom")
    @Mapping(target = "validTo",      expression = "java(nullIfInfinity(entity.getValidTo()))")
    @Mapping(target = "transactionFrom", source = "transactionFrom")
    @Mapping(target = "transactionTo",   expression = "java(nullIfInfinity(entity.getTransactionTo()))")
    PersonAddress toDto(PersonAddressEntity entity);

    List<PersonAddress> toDtoList(List<PersonAddressEntity> entities);

    default AddressType toApiType(PersonAddressEntity.AddressType type) {
        if (type == null) return null;
        return AddressType.fromValue(type.name());
    }

    default OffsetDateTime nullIfInfinity(OffsetDateTime value) {
        return BitemporalEntity.INFINITY.equals(value) ? null : value;
    }

    default String map(UUID value) {
        return value == null ? null : value.toString();
    }
}