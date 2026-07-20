package com.coresuite.crm;

import com.coresuite.shared.auth.AuthHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcBuilderCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.MySQLContainer;

// Nested @TestConfiguration classes are only auto-detected on the exact test
// class JUnit runs, not inherited from a superclass — without this explicit
// @Import, subclasses of this abstract base silently never got the
// authenticated-by-default MockMvc customizer below, and every request came
// back 401/403 with no trusted headers attached at all.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Import(AbstractIntegrationTest.AuthenticatedMockMvcConfig.class)
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

    /**
     * Every controller now requires a trusted identity from api-gateway. The
     * autowired {@code MockMvc} bean carries valid headers on every request by
     * default, so existing tests don't each need to add them — enforcement
     * itself is covered separately in SecurityEnforcementTest, which builds its
     * own MockMvc without this customizer.
     */
    @TestConfiguration
    static class AuthenticatedMockMvcConfig {

        @Bean
        MockMvcBuilderCustomizer authenticatedRequestCustomizer(
                @Value("${coresuite.gateway-secret}") String gatewaySecret) {
            return builder -> builder.defaultRequest(MockMvcRequestBuilders.get("/")
                    .header(AuthHeaders.GATEWAY_SECRET, gatewaySecret)
                    .header(AuthHeaders.USER_ID, "1")
                    .header(AuthHeaders.USER_EMAIL, "test@example.com")
                    .header(AuthHeaders.USER_ROLES, "STAFF"));
        }
    }
}
