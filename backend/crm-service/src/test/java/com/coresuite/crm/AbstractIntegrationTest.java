package com.coresuite.crm;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    // Deliberately not using @Testcontainers/@Container: that combination stops
    // containers after each test class's afterAll, even for static fields in a
    // shared base class, which restarts them mid-suite and breaks the next test
    // class's connection. This is Testcontainers' documented "singleton
    // container" pattern instead — started once, left running, reaped at JVM
    // exit.
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");

    @ServiceConnection
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    static {
        MYSQL.start();
        MONGO.start();
    }
}
