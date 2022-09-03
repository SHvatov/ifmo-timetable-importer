package com.shvatov.ifmo.timetable.importer.service;

import com.shvatov.ifmo.timetable.importer.config.IfmoWebClientConfig.IfmoWebClient;
import com.shvatov.ifmo.timetable.importer.model.IfmoTimetableResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class IfmoTimetableApiClient {

    private static final String IFMO_PERSONAL_SCHEDULE = "/api/schedule/schedule/personal";
    private static final String DATE_START_QUERY_PARAM = "date_start";
    private static final String DATE_END_QUERY_PARAM = "date_end";

    @IfmoWebClient
    private final WebClient ifmoWebClient;

    public Mono<IfmoTimetableResponse> getTimetable(@NonNull LocalDate from, @NonNull LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("\"from\" must be less or equal to \"to\"");
        }

        log.info("Retrieving schedule from IFMO API...");
        return ifmoWebClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .path(IFMO_PERSONAL_SCHEDULE)
                                .queryParam(DATE_START_QUERY_PARAM, from)
                                .queryParam(DATE_END_QUERY_PARAM, to)
                                .build())
                .retrieve()
                .bodyToMono(IfmoTimetableResponse.class)
                .doOnError(error -> log.error("Error while accessing IFMO API", error))
                .doOnNext(it -> log.info("Retrieved following schedule: {}", it));
    }
}
