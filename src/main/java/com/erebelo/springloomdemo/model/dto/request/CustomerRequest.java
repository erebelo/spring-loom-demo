package com.erebelo.springloomdemo.model.dto.request;

import java.time.LocalDate;
import org.apache.commons.csv.CSVRecord;

public record CustomerRequest(String customerId, String firstName, String lastName, String email, Integer age,
        String city, String country, LocalDate registrationDate, Boolean active) {

    public static CustomerRequest fromRecord(CSVRecord csvRecord) {
        return new CustomerRequest(csvRecord.get("customerId"), csvRecord.get("firstName"), csvRecord.get("lastName"),
                csvRecord.get("email"), Integer.parseInt(csvRecord.get("age")), csvRecord.get("city"),
                csvRecord.get("country"), LocalDate.parse(csvRecord.get("registrationDate")),
                Boolean.parseBoolean(csvRecord.get("active")));
    }
}
