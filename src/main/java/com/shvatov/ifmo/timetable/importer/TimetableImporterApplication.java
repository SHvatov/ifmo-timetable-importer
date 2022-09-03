package com.shvatov.ifmo.timetable.importer;

import com.shvatov.ifmo.timetable.importer.config.TimetableProperties;
import com.shvatov.ifmo.timetable.importer.model.CreateTrelloCardWithTemplateRequest;
import com.shvatov.ifmo.timetable.importer.model.IfmoTimetableResponse;
import com.shvatov.ifmo.timetable.importer.model.IfmoTimetableResponse.IfmoLesson;
import com.shvatov.ifmo.timetable.importer.service.IfmoTimetableApiClient;
import com.shvatov.ifmo.timetable.importer.service.TrelloApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
@ConfigurationPropertiesScan(basePackages = "com.shvatov")
public class TimetableImporterApplication implements CommandLineRunner {

    private final IfmoTimetableApiClient ifmoTimetableApiClient;
    private final TrelloApiClient trelloApiClient;
    private final TimetableProperties timetableProperties;

    private record Pair<T, R>(T first, R second) {}

    public static void main(String[] args) {
        SpringApplication.run(TimetableImporterApplication.class, args).close();
    }

    @Override
    public void run(String... args) {
        ifmoTimetableApiClient.getTimetable(timetableProperties.getStartDate(), timetableProperties.getEndDate())
                .doOnNext(it -> log.info("Получено следующее расписание: {}", it))
                .flatMapIterable(IfmoTimetableResponse::getElements)
                .filter(it -> !it.getLessons().isEmpty())
                .flatMapIterable(it ->
                        it.getLessons()
                                .stream()
                                .map(lesson -> new Pair<>(it.getDate(), lesson))
                                .toList())
                .flatMap(pair ->
                        trelloApiClient.createCardWithTemplate(
                                        new CreateTrelloCardWithTemplateRequest()
                                                .setName(timetableCardName(pair.first()))
                                                .setDescription(timetableCardDescription(pair.second()))
                                                .setDueDate(pair.first().atTime(LocalTime.of(22, 0, 0)))
                                                .setTemplateCardId(timetableProperties.getTrelloCardTemplateId())
                                                .setListId(timetableProperties.getTrelloCardDestinationListId()))
                                .doOnNext(it -> log.info("Получен ответ от Trello: {}", it)))
                .blockLast();
    }


    private String timetableCardName(LocalDate date) {
        return "Занятия в университете [%s]".formatted(date);
    }

    private String timetableCardDescription(IfmoLesson lesson) {
        return """
                ### %s
                **Преподаватель:** %s
                **Предмет:**       %s
                **Начало:**        %s
                **Окончание:**     %s
                **Адрес:**         %s
                **Аудитория:**     %s
                \n
                """
                .formatted(
                        lesson.getType(), lesson.getTeacher(),
                        lesson.getSubject(), lesson.getFrom(), lesson.getTo(),
                        lesson.getAddress(), lesson.getRoom());
    }
}
