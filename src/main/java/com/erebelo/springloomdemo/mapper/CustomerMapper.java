package com.erebelo.springloomdemo.mapper;

import static org.mapstruct.ReportingPolicy.WARN;

import com.erebelo.springloomdemo.model.dto.request.CustomerRequest;
import com.erebelo.springloomdemo.model.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", unmappedTargetPolicy = WARN)
public interface CustomerMapper {

    Customer toEntity(CustomerRequest request);

}
