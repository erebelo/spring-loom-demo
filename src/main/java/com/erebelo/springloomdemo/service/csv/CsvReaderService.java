package com.erebelo.springloomdemo.service.csv;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class CsvReaderService {

    public <T> List<T> read(Resource resource, Function<CSVRecord, T> mapper) {

        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get()
                        .parse(reader)) {
            return parser.stream().map(mapper).toList();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read CSV file: " + resource.getDescription(), e);
        }
    }
}
