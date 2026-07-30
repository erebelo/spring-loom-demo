package com.erebelo.springloomdemo.model.entity;

import com.erebelo.springloomdemo.model.enums.BatchProcessor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "batch_failed_records")
public class BatchFailedRecord {

    @Id
    private String id;

    @NotBlank(message = "is mandatory")
    private String executionId;

    @NotNull(message = "is mandatory")
    private BatchProcessor processor;

    @NotBlank(message = "is mandatory")
    private String exceptionMessage;

    @NotBlank(message = "is mandatory")
    private String stackTrace;

    private Object metadata;

}
