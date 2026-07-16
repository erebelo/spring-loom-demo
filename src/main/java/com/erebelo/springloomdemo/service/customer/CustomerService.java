package com.erebelo.springloomdemo.service.customer;

import com.erebelo.springloomdemo.domain.dto.CustomerDto;
import com.erebelo.springloomdemo.domain.model.Customer;
import com.erebelo.springloomdemo.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.ReplaceOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMapper mapper;
    private final MongoTemplate mongoTemplate;

    /**
     * No @Transactional required: replace(...).upsert() is a single atomic MongoDB
     * operation that either completes entirely or fails without requiring an
     * application transaction.
     */
    public void upsert(CustomerDto dto) {
        Customer customer = mapper.toEntity(dto);
        Query query = Query.query(Criteria.where("customerId").is(dto.customerId()));

        /*
         * TODO BaseEntity Performs an atomic, thread-safe upsert.
         *
         * MongoDB executes the replace-or-insert as a single indivisible operation.
         * Multiple Virtual Threads can safely invoke this method concurrently without
         * first checking whether the document exists, avoiding race conditions caused
         * by separate find-and-save operations.
         */
        mongoTemplate.replace(query, customer, ReplaceOptions.replaceOptions().upsert());
    }
}
