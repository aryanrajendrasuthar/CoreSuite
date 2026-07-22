package com.coresuite.gateway.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.coresuite.gateway.AbstractIntegrationTest;
import com.coresuite.gateway.dto.LoginRequest;
import com.coresuite.gateway.dto.RegisterRequest;
import com.coresuite.gateway.dto.TotpCodeRequest;
import com.coresuite.gateway.dto.TotpSetupResponse;
import com.coresuite.gateway.dto.UserResponse;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.time.SystemTimeProvider;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.reactive.server.WebTestClient;

class TotpIntegrationTest extends AbstractIntegrationTest {

    private static final String PASSWORD = "correct horse battery staple";

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void enrollmentEnforcementAndDisableLifecycle() throws Exception {
        String email = uniqueEmail();
        String sessionToken = registerAndLogin(email);

        TotpSetupResponse setup = webTestClient.post().uri("/api/auth/totp/setup")
                .cookie(AuthController.SESSION_COOKIE, sessionToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TotpSetupResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(setup).isNotNull();
        assertThat(setup.otpAuthUri()).startsWith("otpauth://totp/");

        // Wrong code is rejected, TOTP stays disabled.
        webTestClient.post().uri("/api/auth/totp/enable")
                .cookie(AuthController.SESSION_COOKIE, sessionToken)
                .bodyValue(new TotpCodeRequest("000000"))
                .exchange()
                .expectStatus().isUnauthorized();

        webTestClient.post().uri("/api/auth/totp/enable")
                .cookie(AuthController.SESSION_COOKIE, sessionToken)
                .bodyValue(new TotpCodeRequest(currentCode(setup.secret())))
                .exchange()
                .expectStatus().isOk();

        // Login now requires a TOTP code.
        webTestClient.post().uri("/api/auth/login")
                .bodyValue(new LoginRequest(email, PASSWORD, null))
                .exchange()
                .expectStatus().isUnauthorized();

        webTestClient.post().uri("/api/auth/login")
                .bodyValue(new LoginRequest(email, PASSWORD, "000000"))
                .exchange()
                .expectStatus().isUnauthorized();

        ResponseCookie loggedInCookie = webTestClient.post().uri("/api/auth/login")
                .bodyValue(new LoginRequest(email, PASSWORD, currentCode(setup.secret())))
                .exchange()
                .expectStatus().isOk()
                .returnResult(UserResponse.class)
                .getResponseCookies()
                .getFirst(AuthController.SESSION_COOKIE);

        assertThat(loggedInCookie).isNotNull();
        String loggedInToken = loggedInCookie.getValue();

        // Disabling requires a valid code too.
        webTestClient.post().uri("/api/auth/totp/disable")
                .cookie(AuthController.SESSION_COOKIE, loggedInToken)
                .bodyValue(new TotpCodeRequest("000000"))
                .exchange()
                .expectStatus().isUnauthorized();

        webTestClient.post().uri("/api/auth/totp/disable")
                .cookie(AuthController.SESSION_COOKIE, loggedInToken)
                .bodyValue(new TotpCodeRequest(currentCode(setup.secret())))
                .exchange()
                .expectStatus().isOk();

        // TOTP no longer required.
        webTestClient.post().uri("/api/auth/login")
                .bodyValue(new LoginRequest(email, PASSWORD, null))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void totpEndpointsRequireASession() {
        webTestClient.post().uri("/api/auth/totp/setup").exchange().expectStatus().isUnauthorized();
        webTestClient.post().uri("/api/auth/totp/enable")
                .bodyValue(new TotpCodeRequest("123456"))
                .exchange()
                .expectStatus().isUnauthorized();
        webTestClient.post().uri("/api/auth/totp/disable")
                .bodyValue(new TotpCodeRequest("123456"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private String registerAndLogin(String email) {
        webTestClient.post().uri("/api/auth/register")
                .bodyValue(new RegisterRequest(email, PASSWORD))
                .exchange()
                .expectStatus().isCreated();

        ResponseCookie sessionCookie = webTestClient.post().uri("/api/auth/login")
                .bodyValue(new LoginRequest(email, PASSWORD, null))
                .exchange()
                .expectStatus().isOk()
                .returnResult(UserResponse.class)
                .getResponseCookies()
                .getFirst(AuthController.SESSION_COOKIE);

        assertThat(sessionCookie).isNotNull();
        return sessionCookie.getValue();
    }

    private String currentCode(String rawSecret) throws Exception {
        var codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);
        var timeProvider = new SystemTimeProvider();
        long timeCounter = timeProvider.getTime() / 30;
        return codeGenerator.generate(rawSecret, timeCounter);
    }

    private String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }
}
