package com.coresuite.shared.auth;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

/** Argon2id password hashing, per the security baseline in SECURITY.md. */
public final class PasswordHasher {

    private static final Argon2PasswordEncoder ENCODER =
            Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public String hash(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String hashedPassword) {
        return ENCODER.matches(rawPassword, hashedPassword);
    }
}
