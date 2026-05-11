package com.bsoft.timeseries.mapper;

import com.bsoft.timeseries.entity.PersonAddressEntity;

import com.bsoft.timeseries.timeseries.model.AddressType;
import com.bsoft.timeseries.timeseries.model.PersonAddress;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.UUID;

/**
 * Why there is NO local OffsetDateTime helper here:
 * <p>
 * When a mapper declares `uses = AddressMapper.class`, MapStruct brings every
 * public method from AddressMapper into scope as a conversion candidate.
 * If this mapper also declares its own OffsetDateTime→OffsetDateTime method,
 * MapStruct sees TWO candidates for EVERY OffsetDateTime field — including
 * pass-through ones like validFrom — and fails with "ambiguous mapping methods".
 * <p>
 * The solution: annotate the helper in AddressMapper with @Named("nullIfInfinity")
 * so MapStruct never picks it up automatically. Then reference it explicitly via
 * `qualifiedByName = "nullIfInfinity"` only on the two fields that actually need
 * the sentinel→null conversion (validTo, transactionTo). validFrom and
 * transactionFrom are direct pass-through mappings with no conversion needed.
 */
@Mapper(
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = AddressMapper.class,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR // Forceer constructor injectie
)
@Primary
public interface PersonAddressMapper {

    @Mapping(target = "uid", source = "uid")
    @Mapping(target = "personUid", source = "personUid")
    @Mapping(target = "addressUid", source = "addressUid")
    @Mapping(target = "addressType", expression = "java(toApiType(entity.getAddressType()))")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "validFrom", source = "validFrom")
    @Mapping(target = "validTo", source = "validTo", qualifiedByName = "nullIfInfinity")
    @Mapping(target = "transactionFrom", source = "transactionFrom")
    @Mapping(target = "transactionTo", source = "transactionTo", qualifiedByName = "nullIfInfinity")
    PersonAddress toDto(PersonAddressEntity entity);

    List<PersonAddress> toDtoList(List<PersonAddressEntity> entities);

    default AddressType toApiType(PersonAddressEntity.AddressType type) {
        if (type == null) return null;
        return AddressType.fromValue(type.name());
    }

    default String mapUuid(UUID value) {
        return value == null ? null : value.toString();
    }
}