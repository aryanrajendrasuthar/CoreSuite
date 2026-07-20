package com.coresuite.gateway.service;

import com.coresuite.gateway.domain.Role;
import com.coresuite.gateway.domain.User;
import com.coresuite.gateway.dto.LoginRequest;
import com.coresuite.gateway.dto.RegisterRequest;
import com.coresuite.gateway.dto.UserResponse;
import com.coresuite.gateway.repository.UserRepository;
import com.coresuite.shared.auth.PasswordHasher;
import com.coresuite.shared.error.ConflictException;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final PasswordHasher passwordHasher = new PasswordHasher();

    public record LoginResult(String token, UserResponse user) {
    }

    public Mono<UserResponse> register(RegisterRequest request) {
        return Mono.fromCallable(() -> {
                    if (userRepository.existsByEmail(request.email())) {
                        throw new ConflictException("Email already registered: " + request.email());
                    }
                    User user = new User();
                    user.setEmail(request.email());
                    user.setPasswordHash(passwordHasher.hash(request.password()));
                    // First registered user bootstraps as ADMIN; everyone after is STAFF.
                    user.setRoles(Set.of(userRepository.count() == 0 ? Role.ADMIN : Role.STAFF));
                    return UserResponse.from(userRepository.save(user));
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<LoginResult> login(LoginRequest request) {
        return Mono.fromCallable(() -> userRepository.findByEmail(request.email())
                        .filter(user -> passwordHasher.matches(request.password(), user.getPasswordHash()))
                        .orElseThrow(InvalidCredentialsException::new))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(user -> sessionService
                        .createSession(new SessionService.SessionData(
                                user.getId(),
                                user.getEmail(),
                                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet())))
                        .map(token -> new LoginResult(token, UserResponse.from(user))));
    }
}
