package com.bsoft.timeseries.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface JsonNullableMapper {

    default String map(JsonNullable<String> value) {
        if (value == null || !value.isPresent()) {
            return null;
        }
        return value.get();
    }
}