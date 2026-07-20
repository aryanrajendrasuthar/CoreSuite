package com.coresuite.shared.auth;

import java.util.Set;

/** The identity api-gateway attaches to a request after validating the caller's session. */
public record TrustedIdentity(Long userId, String email, Set<String> roles) {
}
