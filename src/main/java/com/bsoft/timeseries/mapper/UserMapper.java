package com.bsoft.timeseries.mapper;

import com.bsoft.timeseries.authentication.model.Role;
import com.bsoft.timeseries.authentication.model.User;
import com.bsoft.timeseries.entity.RolesEntity;
import com.bsoft.timeseries.entity.UserEntity;
import lombok.Setter;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Setter
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        uses = {
                JsonNullableMapper.class
        },
        nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
public abstract class UserMapper implements JsonNullableMapper {

    @Autowired  // Inject the RoleMapper
    private RoleMapper roleMapper;

    @Mapping(target = "accountNonExpired", source = "account_non_expired")
    @Mapping(target = "accountNonLocked", source = "account_non_locked")
    @Mapping(target = "credentialsNonExpired", source = "credentials_non_expired")
    @Mapping(source = "roles", target = "roles", qualifiedByName = "mapToRoles")
    public abstract User map(UserEntity source);

    @Named("mapToRoles")
    public List<Role> roleDTOCollectionToRoleList(Collection<RolesEntity> source) {
        return source.stream()
                .map(roleMapper::map) // Use the injected RoleMapper
                .collect(Collectors.toList());
    }


    @Mapping(target = "account_non_expired", source = "accountNonExpired")
    @Mapping(target = "account_non_locked", source = "accountNonLocked")
    @Mapping(target = "credentials_non_expired", source = "credentialsNonExpired")
    @Mapping(source = "roles", target = "roles", qualifiedByName = "mapToRolesDTO")
    public abstract UserEntity map(User source);

    @Named("mapToRolesDTO")
    public Collection<RolesEntity> roleListToRoleCollection(List<Role> source) {
        return source.stream()
                .map(roleMapper::map) // Use the injected RoleMapper
                .collect(Collectors.toList());
    }

}
