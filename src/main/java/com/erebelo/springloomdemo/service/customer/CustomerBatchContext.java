package com.erebelo.springloomdemo.service.customer;

import com.erebelo.springloomdemo.model.dto.request.CustomerRequest;
import com.erebelo.springloomdemo.model.enums.BatchProcessor;
import com.erebelo.springloomdemo.service.BatchContext;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerBatchContext implements BatchContext<CustomerRequest> {

    private final CustomerService customerService;

    private static final Duration STALE_TIMEOUT = Duration.ofHours(1);

    @Override
    public BatchProcessor processor() {
        return BatchProcessor.CUSTOMER;
    }

    @Override
    public Duration staleTimeout() {
        return STALE_TIMEOUT;
    }

    @Override
    public int csvReadBatchSize() {
        return 5_000;
    }

    @Override
    public Resource resource() {
        return new ClassPathResource("input/customers.csv");
    }

    @Override
    public Function<CSVRecord, CustomerRequest> mapper() {
        return CustomerRequest::fromRecord;
    }

    @Override
    public Consumer<CustomerRequest> persistFunction() {
        return customerService::upsert;
    }

    @Override
    public Function<CustomerRequest, String> recordIdExtractor() {
        return CustomerRequest::customerId;
    }
}
