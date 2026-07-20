package com.coresuite.gateway.dto;

import com.coresuite.gateway.domain.User;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(Long id, String email, Set<String> roles) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
    }
}
