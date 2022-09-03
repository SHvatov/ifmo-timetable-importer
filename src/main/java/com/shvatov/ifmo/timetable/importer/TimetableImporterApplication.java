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
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
@ConfigurationPropertiesScan(basePackages = "com.shvatov")
public class TimetableImporterApplication implements CommandLineRunner {

    private final IfmoTimetableApiClient ifmoTimetableApiClient;
    private final TrelloApiClient trelloApiClient;
    private final TimetableProperties timetableProperties;

    public static void main(String[] args) {
        SpringApplication.run(TimetableImporterApplication.class, args).close();
    }

    @Override
    public void run(String... args) {
        ifmoTimetableApiClient.getTimetable(timetableProperties.getStartDate(), timetableProperties.getEndDate())
                .doOnNext(it -> log.info("Получено следующее расписание: {}", it))
                .flatMapIterable(IfmoTimetableResponse::getElements)
                .filter(it -> !it.getLessons().isEmpty())
                .map(it ->
                        trelloApiClient.createCardWithTemplate(
                                        new CreateTrelloCardWithTemplateRequest()
                                                .setName(timetableCardName(it.getDate()))
                                                .setDescription(timetableCardDescription(it.getLessons()))
                                                .setDueDate(it.getDate().atTime(LocalTime.of(22, 0, 0)))
                                                .setTemplateCardId(timetableProperties.getTrelloCardTemplateId())
                                                .setListId(timetableProperties.getTrelloCardDestinationListId()))
                                .doOnNext(rs -> log.info("Получен ответ от Trello: {}", rs)))
                .blockLast();
    }


    private String timetableCardName(LocalDate date) {
        return "Занятия в университете [%s]".formatted(date);
    }

    private String timetableCardDescription(List<IfmoLesson> lessons) {
        return lessons.stream()
                .map(this::timetableCardDescription)
                .collect(Collectors.joining("\n"));
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
                """
                .formatted(
                        lesson.getType(), lesson.getTeacher(),
                        lesson.getSubject(), lesson.getFrom(), lesson.getTo(),
                        lesson.getAddress(), lesson.getRoom());
    }
}
