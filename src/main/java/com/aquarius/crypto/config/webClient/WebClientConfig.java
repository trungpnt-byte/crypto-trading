package com.aquarius.crypto.config.webClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    private static final int TIMEOUT_MILLIS = 5000;

    private static final int RETRY_COUNT = 1;
    private static final Duration RETRY_MIN_BACKOFF = Duration.ofSeconds(2);

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, TIMEOUT_MILLIS)
                .keepAlive(true)
                .responseTimeout(Duration.ofMillis(TIMEOUT_MILLIS))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS))
                );

        Retry retrySpec = Retry.backoff(RETRY_COUNT, RETRY_MIN_BACKOFF)
                .jitter(0.5)
                .filter(throwable -> {
                    if (throwable instanceof WebClientResponseException) {
                        WebClientResponseException responseException = (WebClientResponseException) throwable;
                        return !responseException.getStatusCode().is4xxClientError();
                    }
                    return true;
                })
                .doBeforeRetry(retrySignal ->
                        System.out.println("Retrying request after " + retrySignal.totalRetries() + " attempt(s)")
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter((request, next) -> next.exchange(request)
                        .retryWhen(retrySpec));
    }
}
