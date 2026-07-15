package com.erebelo.springloomdemo.mapper;

import static org.mapstruct.ReportingPolicy.WARN;

import com.erebelo.springloomdemo.domain.dto.CustomerDto;
import com.erebelo.springloomdemo.domain.model.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = WARN)
public interface CustomerMapper {

    Customer toEntity(CustomerDto dto);

}
