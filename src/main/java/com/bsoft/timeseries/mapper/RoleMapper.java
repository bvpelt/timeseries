package com.bsoft.timeseries.mapper;

import com.bsoft.timeseries.authentication.model.Role;
import com.bsoft.timeseries.entity.RolesEntity;
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
public abstract class RoleMapper implements JsonNullableMapper {

    public abstract Role map(RolesEntity source);

    public abstract RolesEntity map(Role source);

}
