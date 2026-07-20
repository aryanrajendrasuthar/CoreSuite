package com.coresuite.gateway.filter;

import com.coresuite.gateway.service.SessionService;
import com.coresuite.gateway.web.AuthController;
import com.coresuite.shared.auth.AuthHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Requires a valid session for every route except /api/auth/** (login,
 * register, etc., which must be reachable without one). On success, attaches
 * headers only api-gateway can set — the identity the backend services trust.
 */
@Component
public class AuthenticationGatewayFilter implements GlobalFilter, Ordered {

    private final SessionService sessionService;
    private final String gatewaySecret;

    public AuthenticationGatewayFilter(
            SessionService sessionService, @Value("${coresuite.gateway-secret}") String gatewaySecret) {
        this.sessionService = sessionService;
        this.gatewaySecret = gatewaySecret;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path.startsWith("/api/auth/")) {
            return chain.filter(exchange);
        }

        var cookie = exchange.getRequest().getCookies().getFirst(AuthController.SESSION_COOKIE);
        if (cookie == null) {
            return unauthorized(exchange);
        }

        return sessionService.findSession(cookie.getValue())
                .flatMap(session -> {
                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header(AuthHeaders.GATEWAY_SECRET, gatewaySecret)
                            .header(AuthHeaders.USER_ID, String.valueOf(session.userId()))
                            .header(AuthHeaders.USER_EMAIL, session.email())
                            .header(AuthHeaders.USER_ROLES, String.join(",", session.roles()))
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .switchIfEmpty(Mono.defer(() -> unauthorized(exchange)));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
