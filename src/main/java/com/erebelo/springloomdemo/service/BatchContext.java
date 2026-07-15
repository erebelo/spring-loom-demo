package com.erebelo.springloomdemo.service;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.csv.CSVRecord;

public interface BatchContext<T> {

    String processor();

    Path path();

    Function<CSVRecord, T> mapper();

    Consumer<T> persistFunction();

    Function<T, String> recordIdExtractor();

}
