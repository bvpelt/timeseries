package com.bsoft.timeseries.mapper;

import com.bsoft.timeseries.authentication.model.Privilege;
import com.bsoft.timeseries.entity.PrivilegeEntity;
import lombok.Setter;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Setter
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {
                JsonNullableMapper.class
        },
        nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public abstract class PrivilegeMapper implements JsonNullableMapper {

    public abstract Privilege map(PrivilegeEntity privilegeEntity);

    public abstract PrivilegeEntity map(Privilege privilege);

}