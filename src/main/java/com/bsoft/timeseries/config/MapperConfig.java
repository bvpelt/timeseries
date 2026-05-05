package com.bsoft.timeseries.config;

import com.bsoft.timeseries.mapper.AddressMapper;
import com.bsoft.timeseries.mapper.AgreementMapper;
import com.bsoft.timeseries.mapper.PersonMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public AddressMapper addressMapper() {
        // MapStruct provides this factory method when componentModel is NOT spring
        return Mappers.getMapper(AddressMapper.class);
    }

    @Bean
    public AgreementMapper agreementMapper() {
        // MapStruct provides this factory method when componentModel is NOT spring
        return Mappers.getMapper(AgreementMapper.class);
    }

    @Bean
    public PersonMapper personMapper() {
        // MapStruct provides this factory method when componentModel is NOT spring
        return Mappers.getMapper(PersonMapper.class);
    }
}