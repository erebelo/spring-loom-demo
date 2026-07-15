package com.erebelo.springloomdemo.config;

import com.erebelo.springloomdemo.converter.LocalDateReadingConverter;
import com.erebelo.springloomdemo.converter.LocalDateWritingConverter;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(basePackages = "com.erebelo.springloomdemo")
public class MongoBeanConfig {

    /**
     * Enables transaction management for MongoDB operations through @Transactional.
     */
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    /**
     * Enables entity validation before persisting to MongoDB.
     */
    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener(final LocalValidatorFactoryBean factory) {
        return new ValidatingMongoEventListener(factory);
    }

    /**
     * Provides current user info for @CreatedBy and @LastModifiedBy fields used in
     * the BaseEntity.
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("default");
    }

    /**
     * Registers custom converters for MongoDB to serialize and deserialize
     * LocalDate values.
     */
    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(
                Arrays.asList(new LocalDateWritingConverter(), new LocalDateReadingConverter()));
    }
}
