package com.coresuite.shared.auth;

import java.security.SecureRandom;
import java.util.Base64;

/** Generates opaque, unguessable session tokens — 256 bits of randomness, per the security baseline. */
public final class SessionTokens {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int TOKEN_BYTES = 32;

    public String generate() {
        byte[] bytes = new byte[TOKEN_BYTES];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
