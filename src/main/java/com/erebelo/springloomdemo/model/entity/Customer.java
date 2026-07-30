package com.erebelo.springloomdemo.model.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
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
@Document(collection = "customers")
public class Customer extends BaseEntity {

    @Id
    private String id;

    @NotBlank(message = "is mandatory")
    private String customerId;

    @NotBlank(message = "is mandatory")
    private String firstName;

    @NotBlank(message = "is mandatory")
    private String lastName;

    @NotBlank(message = "is mandatory")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "must be valid")
    private String email;

    private Integer age;
    private String city;
    private String country;
    private LocalDate registrationDate;
    private Boolean active;

}
