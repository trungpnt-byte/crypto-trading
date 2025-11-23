package com.aquarius.crypto.integration_test;

import com.aquarius.crypto.model.User;
import com.aquarius.crypto.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.r2dbc.test.autoconfigure.DataR2dbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;

@DataR2dbcTest
@Testcontainers
class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        // Flyway requires the JDBC connection details, not the R2DBC details.
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    @Test
    public void saveUser() {
        // User entity must have all required fields (username, password, email, etc.)
        // Ensure all required fields (like email) are set, as they might be NOT NULL in the schema.
        User u = User.builder().username("testuser").password("testpass").email("blah@gmail.com").build();
        StepVerifier.create(userRepository.save(u))
                .assertNext(savedUser -> {
                    // Verify that an ID has been assigned
                    assert savedUser.getId() != null;
                    assert savedUser.getUsername().equals("testuser");
                    assert savedUser.getPassword().equals("testpass");
                    assert savedUser.getEmail().equals("blah@gmail.com");
                })
                .verifyComplete();
    }


    @Test
    public void findAllUsers() {
        User u1 = User.builder().username("u1").password("p1").email("u1@test.com").build();
        User u2 = User.builder().username("u2").password("p2").email("u2@test.com").build();

        // 1. Save two users
        StepVerifier.create(userRepository.saveAll(List.of(u1, u2)))
                .expectNextCount(2)
                .verifyComplete();

        StepVerifier.create(userRepository.findAll())
                .expectNextCount(4)
                .verifyComplete();
    }
}