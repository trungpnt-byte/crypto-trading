package com.aquarius.crypto.config.tenant;


import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.r2dbc.connection.ConnectionFactoryUtils;
import reactor.core.publisher.Mono;


public record SchemaMultiTenantConnectionProvider(ConnectionFactory delegate) implements ConnectionFactory {
    @NotNull
    @Override
    public Publisher<? extends Connection> create() {
        return Mono.deferContextual(contextView -> {

            String tenantId = TenantContext.getTenantId(contextView);

            return ConnectionFactoryUtils.getConnection(this.delegate)
                    .flatMap(connection -> {
                        String sql = String.format("SET search_path TO %s;", tenantId);

                        return Mono.from(connection.createStatement(sql).execute())
                                .doOnSuccess(result -> {
                                    Mono.from(result.getRowsUpdated()).subscribe();
                                })
                                .thenReturn(connection)
                                .onErrorResume(e -> Mono.from(connection.close()).then(Mono.error(e)));
                    });
        });
    }

    @NotNull
    @Override
    public ConnectionFactoryMetadata getMetadata() {
        return this.delegate.getMetadata();
    }
}
