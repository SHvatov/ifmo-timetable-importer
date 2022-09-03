package com.shvatov.ifmo.timetable.importer.converter;

import lombok.SneakyThrows;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
@ConfigurationPropertiesBinding
public class LocalDateConverter implements Converter<String, LocalDate> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    @SneakyThrows
    public LocalDate convert(String source) {
        return LocalDate.parse(source, formatter);
    }
}