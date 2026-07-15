package com.erebelo.springloomdemo.domain.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "customerId is mandatory")
    private String customerId;

    @NotBlank(message = "firstName is mandatory")
    private String firstName;

    @NotBlank(message = "lastName is mandatory")
    private String lastName;

    @Email
    @NotBlank(message = "email is mandatory")
    private String email;

    private Integer age;
    private String city;
    private String country;
    private LocalDate registrationDate;
    private Boolean active;

}
