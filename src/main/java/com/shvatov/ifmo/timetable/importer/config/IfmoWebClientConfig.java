package com.shvatov.ifmo.timetable.importer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
public class IfmoWebClientConfig {

    private static final String IFMO_WEBCLIENT_BEAN = "ifmoWebClient";

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Qualifier(IFMO_WEBCLIENT_BEAN)
    public @interface IfmoWebClient {}

    @Data
    @ConfigurationProperties(prefix = "ifmo")
    public static class IfmoWebClientConfigProps {

        private String host;
        private String accessToken;
        private Integer timeoutMs;
    }

    private final IfmoWebClientConfigProps properties;

    @Bean(IFMO_WEBCLIENT_BEAN)
    public WebClient ifmoWebClient(ObjectMapper objectMapper) {
        var httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, properties.getTimeoutMs())
                .responseTimeout(Duration.ofMillis(properties.getTimeoutMs()))
                .wiretap("reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL)
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
                .defaultHeader(HttpHeaders.AUTHORIZATION, authorizationHeader())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(strategies)
                .build();
    }

    private String authorizationHeader() {
        return "Bearer " + properties.getAccessToken();
    }
}
