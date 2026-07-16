package com.erebelo.springloomdemo.service.csv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * Reads a CSV file lazily in fixed-size batches.
 * <p>
 * The underlying CSV parser remains open while batches are being consumed and
 * must be closed by the caller using try-with-resources.
 */
public final class CsvBatchReader<T> implements AutoCloseable {

    private final CSVParser parser;
    private final Iterator<CSVRecord> iterator;
    private final Function<CSVRecord, T> mapper;
    private final int batchSize;

    public CsvBatchReader(CSVParser parser, Function<CSVRecord, T> mapper, int batchSize) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException("Batch size must be greater than zero.");
        }

        this.parser = parser;
        this.iterator = parser.iterator();
        this.mapper = mapper;
        this.batchSize = batchSize;
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public List<T> next() {
        List<T> batch = new ArrayList<>(batchSize);

        while (iterator.hasNext() && batch.size() < batchSize) {
            batch.add(mapper.apply(iterator.next()));
        }

        return batch;
    }

    @Override
    public void close() throws IOException {
        // Closing the parser also closes the underlying Reader
        parser.close();
    }
}
