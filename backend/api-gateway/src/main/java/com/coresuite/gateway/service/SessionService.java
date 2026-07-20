package com.coresuite.gateway.service;

import com.coresuite.shared.auth.SessionTokens;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final Duration SESSION_TTL = Duration.ofHours(24);
    private static final String KEY_PREFIX = "session:";

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SessionTokens sessionTokens = new SessionTokens();

    public record SessionData(Long userId, String email, Set<String> roles) {
    }

    public Mono<String> createSession(SessionData data) {
        String token = sessionTokens.generate();
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(data))
                .flatMap(json -> redisTemplate.opsForValue().set(KEY_PREFIX + token, json, SESSION_TTL))
                .thenReturn(token);
    }

    public Mono<SessionData> findSession(String token) {
        if (token == null || token.isBlank()) {
            return Mono.empty();
        }
        return redisTemplate.opsForValue().get(KEY_PREFIX + token)
                .flatMap(json -> {
                    try {
                        return Mono.just(objectMapper.readValue(json, SessionData.class));
                    } catch (JsonProcessingException e) {
                        return Mono.empty();
                    }
                });
    }

    public Mono<Boolean> revokeSession(String token) {
        return redisTemplate.opsForValue().delete(KEY_PREFIX + token);
    }
}
