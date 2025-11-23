package com.aquarius.crypto.config.flyway;

import jakarta.annotation.PostConstruct;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration
public class FlywayConfig {

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
        for (String tenant : getAllTenants()) {
            Flyway flyway = Flyway.configure()
                    .baselineOnMigrate(true)
                    .dataSource(
                            env.getRequiredProperty("spring.flyway.url"),
                            env.getRequiredProperty("spring.flyway.user"),
                            env.getRequiredProperty("spring.flyway.password")
                    )
                    .schemas(tenant)
                    .load();

            flyway.migrate();
        }
    }

    private List<String> getAllTenants() {
        return tenants;
    }

}