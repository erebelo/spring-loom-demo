package com.erebelo.springloomdemo.converter;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class LocalDateReadingConverter implements Converter<Date, LocalDate> {

    @Override
    public LocalDate convert(Date source) {
        return LocalDate.ofInstant(source.toInstant(), ZoneOffset.UTC);
    }
}
