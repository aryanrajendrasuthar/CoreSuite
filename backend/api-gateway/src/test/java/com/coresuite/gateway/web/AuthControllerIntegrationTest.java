package com.coresuite.gateway.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.coresuite.gateway.AbstractIntegrationTest;
import com.coresuite.gateway.dto.LoginRequest;
import com.coresuite.gateway.dto.RegisterRequest;
import com.coresuite.gateway.dto.UserResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.reactive.server.WebTestClient;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void registerLoginMeLogoutLifecycle() {
        String email = uniqueEmail();

        webTestClient.post().uri("/api/auth/register")
                .bodyValue(new RegisterRequest(email, "correct horse battery staple"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponse.class)
                .value(user -> assertThat(user.email()).isEqualTo(email));

        ResponseCookie sessionCookie = webTestClient.post().uri("/api/auth/login")
                .bodyValue(new LoginRequest(email, "correct horse battery staple", null))
                .exchange()
                .expectStatus().isOk()
                .returnResult(UserResponse.class)
                .getResponseCookies()
                .getFirst(AuthController.SESSION_COOKIE);

        assertThat(sessionCookie).isNotNull();
        String token = sessionCookie.getValue();

        webTestClient.get().uri("/api/auth/me")
                .cookie(AuthController.SESSION_COOKIE, token)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(user -> assertThat(user.email()).isEqualTo(email));

        webTestClient.post().uri("/api/auth/logout")
                .cookie(AuthController.SESSION_COOKIE, token)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get().uri("/api/auth/me")
                .cookie(AuthController.SESSION_COOKIE, token)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void registeringTheSameEmailTwiceConflicts() {
        String email = uniqueEmail();
        RegisterRequest request = new RegisterRequest(email, "correct horse battery staple");

        webTestClient.post().uri("/api/auth/register").bodyValue(request).exchange().expectStatus().isCreated();
        webTestClient.post().uri("/api/auth/register").bodyValue(request).exchange().expectStatus().isEqualTo(409);
    }

    @Test
    void loginWithWrongPasswordIsRejected() {
        String email = uniqueEmail();
        webTestClient.post().uri("/api/auth/register")
                .bodyValue(new RegisterRequest(email, "correct horse battery staple"))
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post().uri("/api/auth/login")
                .bodyValue(new LoginRequest(email, "wrong password entirely", null))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void meWithoutASessionIsUnauthorized() {
        webTestClient.get().uri("/api/auth/me").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void protectedRouteRequiresASession() {
        webTestClient.get().uri("/api/products").exchange().expectStatus().isUnauthorized();
    }

    private String uniqueEmail() {
        return "user-" + UUID.randomUUID() + "@example.com";
    }
}
