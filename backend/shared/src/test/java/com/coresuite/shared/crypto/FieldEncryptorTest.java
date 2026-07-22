package com.coresuite.shared.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Base64;
import org.junit.jupiter.api.Test;

class FieldEncryptorTest {

    private static final String KEY = Base64.getEncoder().encodeToString(new byte[32]);

    private final FieldEncryptor encryptor = new FieldEncryptor(KEY);

    @Test
    void encryptsAndDecryptsRoundTrip() {
        String ciphertext = encryptor.encrypt("JBSWY3DPEHPK3PXP");

        assertThat(ciphertext).isNotEqualTo("JBSWY3DPEHPK3PXP");
        assertThat(encryptor.decrypt(ciphertext)).isEqualTo("JBSWY3DPEHPK3PXP");
    }

    @Test
    void sameInputProducesDifferentCiphertextEachTime() {
        String first = encryptor.encrypt("same-value");
        String second = encryptor.encrypt("same-value");

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void rejectsKeyOfWrongLength() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new FieldEncryptor(shortKey))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
