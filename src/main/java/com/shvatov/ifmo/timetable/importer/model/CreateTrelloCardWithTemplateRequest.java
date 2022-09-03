package com.shvatov.ifmo.timetable.importer.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class CreateTrelloCardWithTemplateRequest {

    private String name;
    private String description;
    private LocalDateTime dueDate;
    private String templateCardId;
    private String listId;
}
