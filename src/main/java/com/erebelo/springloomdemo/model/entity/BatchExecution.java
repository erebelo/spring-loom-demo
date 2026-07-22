package com.erebelo.springloomdemo.model.entity;

import com.erebelo.springloomdemo.model.enumertion.BatchStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "batch_executions")
public class BatchExecution extends BaseEntity {

    @Id
    private String id;

    @NotBlank(message = "processor is mandatory")
    private String processor;

    @NotNull(message = "status is mandatory")
    private BatchStatusEnum status;

    @NotNull(message = "startedAt is mandatory")
    private Instant startedAt;

    private Instant lastCheckpointAt;
    private Instant completedAt;
    private Integer successes;
    private Integer failures;
    private String exceptionMessage;
    private String stackTrace;

}
