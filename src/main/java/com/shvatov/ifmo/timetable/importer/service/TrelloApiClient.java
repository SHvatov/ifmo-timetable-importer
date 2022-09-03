package com.shvatov.ifmo.timetable.importer.service;

import com.shvatov.ifmo.timetable.importer.config.TrelloWebClientConfig.TrelloWebClient;
import com.shvatov.ifmo.timetable.importer.model.CreateTrelloCardWithTemplateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrelloApiClient {
    
    private static final String POST_CARDS_V1 = "/1/cards";
    private static final String CARD_NAME = "name";
    private static final String CARD_DESCRIPTION = "desc";
    private static final String CARD_DUE_DATE = "due";
    private static final String CARD_TEMPLATE_ID = "idCardSource";
    private static final String CARD_LIST_ID = "idList";

    @TrelloWebClient
    private final WebClient trelloWebClient;

    public Mono<String> createCardWithTemplate(CreateTrelloCardWithTemplateRequest request) {
        return trelloWebClient.post()
                .uri(
                        uriBuilder -> uriBuilder
                                .path(POST_CARDS_V1)
                                .queryParam("key", "{apiKey}")
                                .queryParam("token", "{apiToken}")
                                .queryParam(CARD_NAME, request.getName())
                                .queryParam(CARD_DESCRIPTION, request.getDescription())
                                .queryParam(CARD_DUE_DATE, request.getDueDate())
                                .queryParam(CARD_TEMPLATE_ID, request.getTemplateCardId())
                                .queryParam(CARD_LIST_ID, request.getListId())
                                .build())
                .retrieve()
                .bodyToMono(String.class) // todo: properly map response?
                .doOnError(error -> log.error("Возникло исключение при обращении к Trello API", error));
    }
}
