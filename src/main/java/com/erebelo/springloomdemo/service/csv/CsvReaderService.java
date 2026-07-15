package com.erebelo.springloomdemo.service.csv;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;

@Service
public class CsvReaderService {

    public <T> List<T> read(Path path, Function<CSVRecord, T> mapper) {

        try (Reader reader = Files.newBufferedReader(path);
                CSVParser parser = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get()
                        .parse(reader)) {

            return parser.stream().map(mapper).toList();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read CSV file: " + path.toAbsolutePath(), e);
        }
    }
}
