package com.tripgoapi;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

// Shared Postgres container for integration tests that need real Postgres semantics (e.g. ON
// CONFLICT, constraint-driven transaction rollback) that H2 does not reproduce faithfully.
// @ServiceConnection wires the container's JDBC URL/credentials into the Spring context
// automatically — no manual @DynamicPropertySource needed.
@Testcontainers
public abstract class AbstractPostgresIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
}
