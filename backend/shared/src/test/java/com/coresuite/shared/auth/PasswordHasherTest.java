package com.coresuite.shared.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    private final PasswordHasher passwordHasher = new PasswordHasher();

    @Test
    void hashesAreNotPlaintextAndVerifyCorrectly() {
        String hash = passwordHasher.hash("correct horse battery staple");

        assertThat(hash).isNotEqualTo("correct horse battery staple");
        assertThat(passwordHasher.matches("correct horse battery staple", hash)).isTrue();
        assertThat(passwordHasher.matches("wrong password", hash)).isFalse();
    }
}
