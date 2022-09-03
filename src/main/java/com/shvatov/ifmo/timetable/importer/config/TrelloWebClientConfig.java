package com.shvatov.ifmo.timetable.importer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class TrelloWebClientConfig {

    private static final String IFMO_WEBCLIENT_BEAN = "trelloWebClient";

    public static final String API_KEY = "apiKey";
    public static final String API_TOKEN = "apiToken";

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier(IFMO_WEBCLIENT_BEAN)
    public @interface TrelloWebClient {}

    @Data
    @ConfigurationProperties(prefix = "trello")
    public static class TrelloWebClientConfigProps {

        private String host;
        private String apiKey;
        private String apiToken;
        private Integer timeoutMs;
    }

    private final TrelloWebClientConfigProps properties;

    @Bean(IFMO_WEBCLIENT_BEAN)
    public WebClient trelloWebClient(ObjectMapper objectMapper) {
        var httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getTimeoutMs())
                .responseTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .doOnConnected(conn -> {
                    conn.addHandlerLast(new ReadTimeoutHandler(properties.getTimeoutMs(), TimeUnit.MILLISECONDS));
                    conn.addHandlerLast(new WriteTimeoutHandler(properties.getTimeoutMs(), TimeUnit.MILLISECONDS));
                });

        var strategies = ExchangeStrategies.builder()
                .codecs(configurer -> {
                    configurer.defaultCodecs()
                            .jackson2JsonEncoder(
                                    new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                    configurer.defaultCodecs()
                            .jackson2JsonDecoder(
                                    new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON));
                }).build();

        return WebClient.builder()
                .baseUrl(properties.getHost())
                .defaultUriVariables(
                        Map.of(
                                API_KEY, properties.getApiKey(),
                                API_TOKEN, properties.getApiToken()))
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }
}
