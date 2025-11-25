package com.aquarius.crypto.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class ReactiveCorrelationIdFilter implements WebFilter {

    public static final String CORRELATION_ID_HEADER_NAME = "X-Correlation-ID";
    public static final String CORRELATION_ID_CONTEXT_KEY = "correlationId";

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String existingId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER_NAME);
        final String correlationId = (existingId != null && !existingId.isEmpty())
                ? existingId
                : generateId();
        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER_NAME, correlationId);

        Mono<Void> filteredChain = chain.filter(exchange);

        return Mono.deferContextual(Mono::just)
                .flatMap(context -> filteredChain.contextWrite(Context.of(CORRELATION_ID_CONTEXT_KEY, correlationId)))
//                .contextWrite(Context.of(CORRELATION_ID_CONTEXT_KEY, correlationId))
                .doOnEach(signal -> {
                    if (signal.getContextView().hasKey(CORRELATION_ID_CONTEXT_KEY)) {
                        MDC.put(CORRELATION_ID_CONTEXT_KEY, signal.getContextView().get(CORRELATION_ID_CONTEXT_KEY));
                    }
                })
                .doFinally(signalType -> {
                    MDC.remove(CORRELATION_ID_CONTEXT_KEY);
                });
    }
}