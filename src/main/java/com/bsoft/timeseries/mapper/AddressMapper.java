package com.bsoft.timeseries.mapper;

import com.bsoft.timeseries.entity.AddressEntity;
import com.bsoft.timeseries.entity.BitemporalEntity;
import com.bsoft.timeseries.model.Address;
import com.bsoft.timeseries.model.AddressRequest;
import org.mapstruct.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// See PersonMapper for explanation of @BeanMapping(builder = @Builder(disableBuilder = true))
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {

    @Mapping(target = "uid", source = "uid")
    @Mapping(target = "validFrom", source = "validFrom")
    @Mapping(target = "validTo", expression = "java(nullIfInfinity(entity.getValidTo()))")
    @Mapping(target = "transactionFrom", source = "transactionFrom")
    @Mapping(target = "transactionTo", expression = "java(nullIfInfinity(entity.getTransactionTo()))")
    Address toDto(AddressEntity entity);

    List<Address> toDtoList(List<AddressEntity> entities);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "transactionFrom", ignore = true)
    @Mapping(target = "transactionTo", ignore = true)
    AddressEntity toNewEntity(AddressRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "transactionFrom", ignore = true)
    @Mapping(target = "transactionTo", ignore = true)
    void updateEntity(AddressRequest request, @MappingTarget AddressEntity entity);

    @Named("nullIfInfinity")
    default OffsetDateTime nullIfInfinity(OffsetDateTime value) {
        return BitemporalEntity.INFINITY.equals(value) ? null : value;
    }

    default String mapUuid(UUID value) {
        return value == null ? null : value.toString();
    }
}