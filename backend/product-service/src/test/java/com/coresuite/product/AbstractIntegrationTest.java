package com.coresuite.product;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {

    // Deliberately not using @Testcontainers/@Container: that combination stops
    // the container after each test class's afterAll, even for a static field in
    // a shared base class, which restarts it mid-suite and breaks the next test
    // class's connection. This is Testcontainers' documented "singleton
    // container" pattern instead — started once, left running, reaped at JVM
    // exit.
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");

    static {
        MYSQL.start();
    }
}
