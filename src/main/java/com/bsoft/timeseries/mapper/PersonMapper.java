package com.bsoft.timeseries.mapper;

import com.bsoft.timeseries.entity.BitemporalEntity;
import com.bsoft.timeseries.entity.PersonEntity;
import com.bsoft.timeseries.model.Person;
import com.bsoft.timeseries.model.PersonRequest;
import org.mapstruct.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * dateOfBirth: the OpenAPI spec declares it as `type: string, format: date`.
 * With dateLibrary=java8 the generator produces LocalDate in both Person and
 * PersonRequest, so NO conversion expression is needed — MapStruct handles
 * LocalDate→LocalDate as a direct copy.
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonMapper {

    @Mapping(target = "uid",             source = "uid")
    @Mapping(target = "dateOfBirth",     source = "dateOfBirth")
    @Mapping(target = "validFrom",       source = "validFrom")
    @Mapping(target = "validTo",         source = "validTo",         qualifiedByName = "nullIfInfinity")
    @Mapping(target = "transactionFrom", source = "transactionFrom")
    @Mapping(target = "transactionTo",   source = "transactionTo",   qualifiedByName = "nullIfInfinity")
    Person toDto(PersonEntity entity);

    List<Person> toDtoList(List<PersonEntity> entities);

    /**
     * Uses setters (not the Lombok builder) so MapStruct can set inherited
     * BitemporalEntity fields (uid, validFrom, …). Lombok @Builder on a
     * subclass only generates builder methods for fields declared in that
     * subclass itself.
     */
    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "uid",             ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "transactionFrom", ignore = true)
    @Mapping(target = "transactionTo",   ignore = true)
    @Mapping(target = "dateOfBirth",     source = "dateOfBirth")
    PersonEntity toNewEntity(PersonRequest request);

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "uid",             ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "transactionFrom", ignore = true)
    @Mapping(target = "transactionTo",   ignore = true)
    @Mapping(target = "dateOfBirth",     source = "dateOfBirth")
    void updateEntity(PersonRequest request, @MappingTarget PersonEntity entity);

    @Named("nullIfInfinity")
    default OffsetDateTime nullIfInfinity(OffsetDateTime value) {
        return BitemporalEntity.INFINITY.equals(value) ? null : value;
    }

    default String mapUuid(UUID value) {
        return value == null ? null : value.toString();
    }
}