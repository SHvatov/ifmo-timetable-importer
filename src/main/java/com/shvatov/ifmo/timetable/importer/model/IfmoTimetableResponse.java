package com.shvatov.ifmo.timetable.importer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IfmoTimetableResponse {

    @JsonProperty("data")
    private List<IfmoTimetableElement> elements;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IfmoTimetableElement {

        @JsonProperty("date")
        private LocalDate date;

        @JsonProperty("lessons")
        private List<IfmoLesson> lessons;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IfmoLesson {

        @JsonProperty("subject")
        private String subject;

        @JsonProperty("type")
        private String type;

        @JsonProperty("time_start")
        private LocalTime from;

        @JsonProperty("time_end")
        private LocalTime to;

        @JsonProperty("teacher_name")
        private String teacher;

        @JsonProperty("room")
        private String room;

        @JsonProperty("building")
        private String address;
    }
}
