package com.erebelo.springloomdemo.domain.model;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "executionId is mandatory")
    private String executionId;

    @NotBlank(message = "processor is mandatory")
    private String processor;

    @NotBlank(message = "exceptionMessage is mandatory")
    private String exceptionMessage;

    @NotBlank(message = "stackTrace is mandatory")
    private String stackTrace;

    private Object metadata;

}
