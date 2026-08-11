package com.erebelo.springloomdemo.model.entity;

import com.erebelo.springloomdemo.model.enums.BatchProcessorName;
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

    @NotBlank
    private String executionId;

    @NotNull
    private BatchProcessorName processor;

    @NotBlank
    private String exceptionMessage;

    @NotBlank
    private String stackTrace;

    private Object metadata;

}
