package com.erebelo.springloomdemo.service.customer;

import com.erebelo.springloomdemo.domain.dto.CustomerDto;
import com.erebelo.springloomdemo.service.BatchContext;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerBatchContext implements BatchContext<CustomerDto> {

    private final CustomerService customerService;

    @Override
    public String processor() {
        return "Customer";
    }

    @Override
    public Resource resource() {
        return new ClassPathResource("input/customers.csv");
    }

    @Override
    public Function<CSVRecord, CustomerDto> mapper() {
        return CustomerDto::fromRecord;
    }

    @Override
    public Consumer<CustomerDto> persistFunction() {
        return customerService::upsert;
    }

    @Override
    public Function<CustomerDto, String> recordIdExtractor() {
        return CustomerDto::customerId;
    }
}
