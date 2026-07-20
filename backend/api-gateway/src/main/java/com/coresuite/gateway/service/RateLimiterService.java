package com.coresuite.gateway.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/** Fixed-window rate limiting backed by Redis — used to throttle auth endpoints. */
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final ReactiveStringRedisTemplate redisTemplate;

    public Mono<Boolean> tryConsume(String key, long maxAttempts, Duration window) {
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    Mono<Boolean> ensureExpiry = count == 1 ? redisTemplate.expire(key, window) : Mono.just(true);
                    return ensureExpiry.thenReturn(count <= maxAttempts);
                });
    }
}
