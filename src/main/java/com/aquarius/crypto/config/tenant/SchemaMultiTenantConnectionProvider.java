package com.aquarius.crypto.config.tenant;


import org.hibernate.engine.jdbc.connections.internal.DriverManagerConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

@Component
@SuppressWarnings("serial")
public class SchemaMultiTenantConnectionProvider extends AbstractMultiTenantConnectionProvider<String> {

    private static final String HIBERNATE_PROPERTIES_PATH = "/application-dev.properties";
    private final Map<String, ConnectionProvider> connectionProviderMap = new HashMap<>();

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = super.getConnection(tenantIdentifier);
        connection.createStatement()
                .execute(String.format("SET SCHEMA '%s';", tenantIdentifier));
        return connection;
    }

    @Override
    public ConnectionProvider getAnyConnectionProvider() {
        return getConnectionProvider(TenantContext.DEFAULT_TENANT_ID);
    }

    @Override
    protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
        return getConnectionProvider(tenantIdentifier);
    }

    private ConnectionProvider getConnectionProvider(String tenantIdentifier) {
        return Optional.ofNullable(tenantIdentifier)
                .map(connectionProviderMap::get)
                .orElseGet(() -> createNewConnectionProvider(tenantIdentifier));
    }

    private ConnectionProvider createNewConnectionProvider(String tenantIdentifier) {
        return Optional.ofNullable(tenantIdentifier)
                .map(this::createConnectionProvider)
                .map(provider -> {
                    connectionProviderMap.put(tenantIdentifier, provider);
                    return provider;
                })
                .orElseThrow(() -> new ConnectionProviderException(
                        String.format("Cannot create new connection provider for tenant: %s", tenantIdentifier)
                ));
    }

    private ConnectionProvider createConnectionProvider(String tenantIdentifier) {
        Properties props = getHibernatePropertiesForTenantId(tenantIdentifier);
        return initConnectionProvider(props);
    }

    private Properties getHibernatePropertiesForTenantId(String tenantId) {
        try {
            Properties properties = new Properties();
            properties.load(getClass().getResourceAsStream(HIBERNATE_PROPERTIES_PATH));
            return properties;
        } catch (IOException e) {
            throw new ConnectionProviderException(
                    String.format("Cannot open hibernate properties: %s", HIBERNATE_PROPERTIES_PATH),
                    e
            );
        }
    }

    private ConnectionProvider initConnectionProvider(Properties hibernateProperties) {
        DriverManagerConnectionProviderImpl provider = new DriverManagerConnectionProviderImpl();

        Map<String, Object> map = new HashMap<>();
        hibernateProperties.forEach((k, v) -> map.put(k.toString(), v.toString()));

        provider.configure(map);
        return provider;
    }
}
