package com.coresuite.gateway.service;

import com.coresuite.shared.crypto.FieldEncryptor;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrDataFactory;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * TOTP (RFC 6238) enrollment and verification. Secrets are stored encrypted
 * (via {@link FieldEncryptor}) and only ever decrypted in memory to verify a code.
 */
@Service
public class TotpService {

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final QrDataFactory qrDataFactory = new QrDataFactory(HashingAlgorithm.SHA1, 6, 30);
    private final CodeVerifier codeVerifier =
            new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());
    private final FieldEncryptor fieldEncryptor;
    private final String issuer;

    public TotpService(
            @Value("${coresuite.field-encryption-key}") String fieldEncryptionKey,
            @Value("${coresuite.totp.issuer}") String issuer) {
        this.fieldEncryptor = new FieldEncryptor(fieldEncryptionKey);
        this.issuer = issuer;
    }

    public record Enrollment(String rawSecret, String encryptedSecret, String otpAuthUri) {}

    public Enrollment generateEnrollment(String userEmail) {
        String rawSecret = secretGenerator.generate();
        String uri = qrDataFactory.newBuilder()
                .label(userEmail)
                .secret(rawSecret)
                .issuer(issuer)
                .build()
                .getUri();
        return new Enrollment(rawSecret, fieldEncryptor.encrypt(rawSecret), uri);
    }

    public boolean isValidCode(String encryptedSecret, String code) {
        if (encryptedSecret == null || code == null || code.isBlank()) {
            return false;
        }
        return codeVerifier.isValidCode(fieldEncryptor.decrypt(encryptedSecret), code);
    }
}
