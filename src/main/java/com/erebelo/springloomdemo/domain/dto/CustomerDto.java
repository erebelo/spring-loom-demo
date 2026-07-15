package com.erebelo.springloomdemo.domain.dto;

import java.time.LocalDate;
import org.apache.commons.csv.CSVRecord;

public record CustomerDto(String customerId, String firstName, String lastName, String email, Integer age, String city,
        String country, LocalDate registrationDate, Boolean active) {

    public static CustomerDto fromRecord(CSVRecord csvRecord) {
        return new CustomerDto(csvRecord.get("customerId"), csvRecord.get("firstName"), csvRecord.get("lastName"),
                csvRecord.get("email"), Integer.parseInt(csvRecord.get("age")), csvRecord.get("city"),
                csvRecord.get("country"), LocalDate.parse(csvRecord.get("registrationDate")),
                Boolean.parseBoolean(csvRecord.get("active")));
    }
}
