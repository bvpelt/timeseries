package com.bsoft.timeseries.mapper;

import com.bsoft.timeseries.entity.AgreementEntity;
import com.bsoft.timeseries.entity.BitemporalEntity;
import com.bsoft.timeseries.model.Agreement;
import com.bsoft.timeseries.model.AgreementRequest;
import com.bsoft.timeseries.model.AgreementState;
import org.mapstruct.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// See PersonMapper for explanation of @BeanMapping(builder = @Builder(disableBuilder = true))
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AgreementMapper {

    @Mapping(target = "uid", source = "uid")
    @Mapping(target = "state", expression = "java(toApiState(entity.getState()))")
    @Mapping(target = "validFrom", source = "validFrom")
    @Mapping(target = "validTo", expression = "java(nullIfInfinity(entity.getValidTo()))")
    @Mapping(target = "transactionFrom", source = "transactionFrom")
    @Mapping(target = "transactionTo", expression = "java(nullIfInfinity(entity.getTransactionTo()))")
    Agreement toDto(AgreementEntity entity);

    List<Agreement> toDtoList(List<AgreementEntity> entities);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "transactionFrom", ignore = true)
    @Mapping(target = "transactionTo", ignore = true)
    @Mapping(target = "state", expression = "java(toEntityState(request.getState()))")
    AgreementEntity toNewEntity(AgreementRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "uid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "transactionFrom", ignore = true)
    @Mapping(target = "transactionTo", ignore = true)
    @Mapping(target = "state", expression = "java(toEntityState(request.getState()))")
    void updateEntity(AgreementRequest request, @MappingTarget AgreementEntity entity);

    default AgreementState toApiState(AgreementEntity.State state) {
        if (state == null) return null;
        return AgreementState.fromValue(state.name());
    }

    default AgreementEntity.State toEntityState(AgreementState state) {
        if (state == null) return AgreementEntity.State.NEW;
        return AgreementEntity.State.valueOf(state.getValue());
    }

    @Named("nullIfInfinity")
    default OffsetDateTime nullIfInfinity(OffsetDateTime value) {
        return BitemporalEntity.INFINITY.equals(value) ? null : value;
    }

    default String mapUuid(UUID value) {
        return value == null ? null : value.toString();
    }
}