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

@DataR2dbcTest
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void setDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    @Test
    public void saveUser() {
        User u = User.builder().username("testuser").password("testpass").email("blah@gmail.com").build();
        StepVerifier.create(userRepository.save(u))
                .assertNext(savedUser -> {
                    assert savedUser.getId() != null;
                    assert savedUser.getUsername().equals("testuser");
                    assert savedUser.getPassword().equals("testpass");
                    assert savedUser.getEmail().equals("blah@gmail.com");
//                    assert reverseParseUUID(savedUser.getPublicId());
                })
                .verifyComplete();
    }
}