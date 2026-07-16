package com.erebelo.springloomdemo.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertCallback;
import org.springframework.stereotype.Component;

/**
 * Validates MongoDB domain objects before they are converted and persisted.
 * Applies to repository and MongoTemplate save/insert operations, but not to
 * update/upsert operations using Update or AggregationUpdate.
 */
@Component
@RequiredArgsConstructor
public class MongoValidationConfig implements BeforeConvertCallback<@NonNull Object> {

    private final Validator validator;

    @Override
    public Object onBeforeConvert(Object entity, @NonNull String collection) {
        Set<ConstraintViolation<Object>> violations = validator.validate(entity);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return entity;
    }
}
