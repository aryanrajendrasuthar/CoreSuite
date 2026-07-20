package com.coresuite.gateway;

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public abstract class AbstractIntegrationTest {

    // Deliberately not using @Testcontainers/@Container: that combination stops
    // containers after each test class's afterAll, even for a static field in
    // a shared base class, which restarts them mid-suite and breaks the next
    // test class's connection. This is Testcontainers' documented "singleton
    // container" pattern instead — started once, left running, reaped at JVM
    // exit.
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");

    @ServiceConnection("redis")
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    static {
        MYSQL.start();
        REDIS.start();
    }
}
