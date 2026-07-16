package com.erebelo.springloomdemo.domain.model;

import com.erebelo.springloomdemo.domain.enumertion.BatchStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
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
    private BatchStatus status;

    @NotNull(message = "startedAt is mandatory")
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
    private Integer successes;
    private Integer failures;
    private String exceptionMessage;
    private String stackTrace;

}
