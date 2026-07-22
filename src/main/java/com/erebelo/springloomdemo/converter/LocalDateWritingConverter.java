package com.erebelo.springloomdemo.converter;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Date;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class LocalDateWritingConverter implements Converter<@NonNull LocalDate, Date> {

    @Override
    public Date convert(LocalDate source) {
        return Date.from(source.atStartOfDay().atOffset(ZoneOffset.UTC).toInstant());
    }
}
