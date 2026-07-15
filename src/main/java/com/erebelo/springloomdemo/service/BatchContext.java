package com.erebelo.springloomdemo.service;

import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.Resource;

public interface BatchContext<T> {

    String processor();

    Resource resource();

    Function<CSVRecord, T> mapper();

    Consumer<T> persistFunction();

    Function<T, String> recordIdExtractor();

}
