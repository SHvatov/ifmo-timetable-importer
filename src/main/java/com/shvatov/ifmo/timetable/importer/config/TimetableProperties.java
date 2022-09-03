package com.shvatov.ifmo.timetable.importer.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.LocalDate;

@Data
@ConfigurationProperties(prefix = "timetable")
public class TimetableProperties {

    private LocalDate startDate;
    private LocalDate endDate;
    private String trelloCardTemplateId;
    private String trelloCardDestinationListId;
}
