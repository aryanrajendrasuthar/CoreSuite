package com.coresuite.crm.web;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coresuite.crm.AbstractIntegrationTest;
import com.coresuite.shared.auth.AuthHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

/**
 * Builds its own MockMvc — deliberately bypassing AbstractIntegrationTest's
 * default-authenticated-headers customizer — to prove requests are actually
 * rejected without a valid trusted identity, not just that authenticated
 * requests happen to work. {@code springSecurity()} is required here: the
 * Spring Security filter chain is only wired into MockMvc automatically for
 * the auto-configured bean, not a manually built one.
 */
class SecurityEnforcementTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc unauthenticatedMockMvc;

    @BeforeEach
    void setUp() {
        unauthenticatedMockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void requestWithNoHeadersIsRejected() throws Exception {
        unauthenticatedMockMvc.perform(get("/api/customers")).andExpect(status().isUnauthorized());
    }

    @Test
    void requestWithWrongGatewaySecretIsRejected() throws Exception {
        unauthenticatedMockMvc.perform(get("/api/customers")
                        .header(AuthHeaders.GATEWAY_SECRET, "not-the-real-secret")
                        .header(AuthHeaders.USER_ID, "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthEndpointIsPubliclyReachable() throws Exception {
        unauthenticatedMockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }
}
