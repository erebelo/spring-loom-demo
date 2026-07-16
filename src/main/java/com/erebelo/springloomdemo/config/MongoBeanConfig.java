package com.erebelo.springloomdemo.config;

import com.erebelo.springloomdemo.converter.LocalDateReadingConverter;
import com.erebelo.springloomdemo.converter.LocalDateWritingConverter;
import java.util.Optional;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

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
     * Provides current user info for @CreatedBy and @LastModifiedBy fields used in
     * the BaseEntity.
     */
    @Bean
    public AuditorAware<@NonNull String> auditorProvider() {
        return () -> Optional.of("default");
    }

    /**
     * Registers custom converters for MongoDB to serialize and deserialize
     * LocalDate values.
     */
    @Bean
    public MongoCustomConversions customConversions() {
        return MongoCustomConversions.create(adapter -> {
            adapter.registerConverter(new LocalDateWritingConverter());
            adapter.registerConverter(new LocalDateReadingConverter());
        });
    }
}
