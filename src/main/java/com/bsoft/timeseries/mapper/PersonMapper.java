package com.bsoft.timeseries.mapper;

import com.bsoft.timeseries.entity.PersonEntity;
import com.bsoft.timeseries.model.Person;
import com.bsoft.timeseries.model.PersonRequest;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * MapStruct mapper converting between the JPA {@link PersonEntity} and
 * the OpenAPI-generated {@link Person} / {@link PersonRequest} DTOs.
 *
 * <p>The {@code componentModel = "spring"} is set globally via the compiler
 * arg {@code -Amapstruct.defaultComponentModel=spring} in pom.xml.</p>
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PersonMapper {

    // ------------------------------------------------------------------
    // Entity → DTO
    // ------------------------------------------------------------------

    @Mapping(target = "uid",             source = "uid")
    @Mapping(target = "firstName",       source = "firstName")
    @Mapping(target = "lastName",        source = "lastName")
    @Mapping(target = "dateOfBirth",     source = "dateOfBirth")
    @Mapping(target = "validFrom",       source = "validFrom")
    @Mapping(target = "validTo",         expression = "java(toNullableOffsetDateTime(entity.getValidTo()))")
    @Mapping(target = "transactionFrom", source = "transactionFrom")
    @Mapping(target = "transactionTo",   expression = "java(toNullableOffsetDateTime(entity.getTransactionTo()))")
    Person toDto(PersonEntity entity);

    List<Person> toDtoList(List<PersonEntity> entities);

    // ------------------------------------------------------------------
    // Request → new Entity (for CREATE)
    // ------------------------------------------------------------------

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "uid",             ignore = true)   // generated in @PrePersist
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "transactionFrom", ignore = true)   // set by @PrePersist
    @Mapping(target = "transactionTo",   ignore = true)
    @Mapping(target = "validFrom",       source = "validFrom")
    @Mapping(target = "validTo",         source = "validTo")
    PersonEntity toNewEntity(PersonRequest request);

    // ------------------------------------------------------------------
    // Request → update existing Entity (for UPDATE)
    // Only business attributes are copied; temporal fields are managed
    // by the service layer (new transaction version).
    // ------------------------------------------------------------------

    @Mapping(target = "id",              ignore = true)
    @Mapping(target = "uid",             ignore = true)
    @Mapping(target = "createdAt",       ignore = true)
    @Mapping(target = "transactionFrom", ignore = true)
    @Mapping(target = "transactionTo",   ignore = true)
    @Mapping(target = "validFrom",       source = "validFrom")
    @Mapping(target = "validTo",         source = "validTo")
    void updateEntity(PersonRequest request, @MappingTarget PersonEntity entity);

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Converts the sentinel infinity value to null for the API response. */
    default OffsetDateTime toNullableOffsetDateTime(OffsetDateTime value) {
        return PersonEntity.INFINITY.equals(value) ? null : value;
    }

    default String map(LocalDate value) {
        return value == null ? null : value.toString();
    }

    default LocalDate map(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    default String map(UUID value) {
        return value == null ? null : value.toString();
    }
}