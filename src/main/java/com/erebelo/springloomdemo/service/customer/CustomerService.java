package com.erebelo.springloomdemo.service.customer;

import com.erebelo.springloomdemo.domain.dto.CustomerDto;
import com.erebelo.springloomdemo.domain.model.Customer;
import com.erebelo.springloomdemo.mapper.CustomerMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.time.Instant;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationUpdate;
import org.springframework.data.mongodb.core.aggregation.ArithmeticOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMapper mapper;
    private final Validator validator;
    private final MongoTemplate mongoTemplate;

    /**
     * No @Transactional required: replace(...).upsert() is a single atomic MongoDB
     * operation that either completes entirely or fails without requiring an
     * application transaction.
     */
    public void upsert(CustomerDto dto) {
        Customer customer = mapper.toEntity(dto);

        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        Query query = Query.query(Criteria.where("customerId").is(dto.customerId()));

        Instant now = Instant.now();

        AggregationUpdate update = AggregationUpdate.update().set("customerId").toValue(customer.getCustomerId())
                .set("firstName").toValue(customer.getFirstName()).set("lastName").toValue(customer.getLastName())
                .set("email").toValue(customer.getEmail()).set("age").toValue(customer.getAge()).set("city")
                .toValue(customer.getCity()).set("country").toValue(customer.getCountry()).set("registrationDate")
                .toValue(customer.getRegistrationDate()).set("active").toValue(customer.getActive())

                /*
                 * Manually manages audit fields and version since MongoDB upsert bypasses
                 * Spring Data auditing and @Version handling.
                 *
                 * Initializes values for new documents and preserves/increments values for
                 * updates.
                 */
                .set("createdBy").toValue(ConditionalOperators.ifNull("createdBy").then("default")).set("modifiedBy")
                .toValue("default").set("createdAt").toValue(ConditionalOperators.ifNull("createdAt").then(now))
                .set("modifiedAt").toValue(now).set("version")
                .toValue(ArithmeticOperators.Add.valueOf(ConditionalOperators.ifNull("version").then(-1)).add(1));

        /*
         * Performs an atomic MongoDB upsert operation.
         *
         * MongoDB executes the update-or-insert as a single operation, allowing
         * multiple Virtual Threads to safely execute concurrently without a previous
         * read check. This avoids race conditions caused by separate find-and-save
         * operations.
         */
        mongoTemplate.upsert(query, update, Customer.class);
    }
}
