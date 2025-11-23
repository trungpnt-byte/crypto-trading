package com.aquarius.crypto.config.flyway;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;

import java.util.List;

@Configuration
public class FlywayConfig {

    private static final String POSTGRES_DRIVER = "org.postgresql.Driver";

    private final List<String> tenants;
    private final Environment env;

    public FlywayConfig(
            @Value("${spring.flyway.schemas}") List<String> tenants,
            Environment env) {
        this.tenants = tenants;
        this.env = env;
    }

    @PostConstruct
    public void migrateAllTenants() {
        String url = env.getRequiredProperty("spring.flyway.url");
        String user = env.getRequiredProperty("spring.flyway.user");
        String password = env.getRequiredProperty("spring.flyway.password");


        for (String tenant : getAllTenants()) {

            Flyway flyway = Flyway.configure()
                    .baselineOnMigrate(true)
                    .driver(POSTGRES_DRIVER)
                    .dataSource(url, user, password)
                    .schemas(tenant)
                    .load();

            System.out.println("Starting Flyway migration for tenant: " + tenant);
            flyway.migrate();
            System.out.println("Finished Flyway migration for tenant: " + tenant);
        }
    }

    private List<String> getAllTenants() {
        return tenants;
    }
}