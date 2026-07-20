package com.coresuite.gateway.web;

import com.coresuite.gateway.dto.LoginRequest;
import com.coresuite.gateway.dto.RegisterRequest;
import com.coresuite.gateway.dto.UserResponse;
import com.coresuite.gateway.service.AuthService;
import com.coresuite.gateway.service.RateLimitExceededException;
import com.coresuite.gateway.service.RateLimiterService;
import com.coresuite.gateway.service.SessionService;
import jakarta.validation.Valid;
import java.net.InetSocketAddress;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    public static final String SESSION_COOKIE = "coresuite_session";
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(15);
    private static final long LOGIN_MAX_ATTEMPTS = 10;

    private final AuthService authService;
    private final SessionService sessionService;
    private final RateLimiterService rateLimiterService;

    @PostMapping("/register")
    public Mono<ResponseEntity<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request).map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<UserResponse>> login(
            @Valid @RequestBody LoginRequest request, ServerHttpRequest httpRequest, ServerWebExchange exchange) {
        String rateLimitKey = "ratelimit:login:" + clientIp(httpRequest);
        return rateLimiterService.tryConsume(rateLimitKey, LOGIN_MAX_ATTEMPTS, LOGIN_WINDOW)
                .flatMap(allowed -> allowed ? authService.login(request) : Mono.error(new RateLimitExceededException()))
                .map(result -> {
                    exchange.getResponse().addCookie(sessionCookie(result.token(), Duration.ofHours(24)));
                    return ResponseEntity.ok(result.user());
                });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange) {
        String token = extractToken(exchange);
        return sessionService.revokeSession(token)
                .doOnNext(ignored -> exchange.getResponse().addCookie(sessionCookie("", Duration.ZERO)))
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<UserResponse>> me(ServerWebExchange exchange) {
        String token = extractToken(exchange);
        return sessionService.findSession(token)
                .map(session -> ResponseEntity.ok(new UserResponse(session.userId(), session.email(), session.roles())))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private String extractToken(ServerWebExchange exchange) {
        var cookie = exchange.getRequest().getCookies().getFirst(SESSION_COOKIE);
        return cookie == null ? null : cookie.getValue();
    }

    private ResponseCookie sessionCookie(String value, Duration maxAge) {
        return ResponseCookie.from(SESSION_COOKIE, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

    private String clientIp(ServerHttpRequest request) {
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress == null ? "unknown" : remoteAddress.getHostString();
    }
}
