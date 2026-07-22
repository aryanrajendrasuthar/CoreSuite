package com.coresuite.gateway.service;

import static org.assertj.core.api.Assertions.assertThat;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.time.SystemTimeProvider;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class TotpServiceTest {

    private static final String FIELD_ENCRYPTION_KEY = Base64.getEncoder().encodeToString(new byte[32]);

    private final TotpService totpService = new TotpService(FIELD_ENCRYPTION_KEY, "CoreSuite");

    @Test
    void enrollmentUriEncodesLabelAndIssuer() {
        TotpService.Enrollment enrollment = totpService.generateEnrollment("user@example.com");

        assertThat(enrollment.otpAuthUri()).startsWith("otpauth://totp/user%40example.com");
        assertThat(enrollment.otpAuthUri()).contains("issuer=CoreSuite");
        assertThat(enrollment.encryptedSecret()).isNotEqualTo(enrollment.rawSecret());
    }

    @Test
    void validCodeForCurrentTimeStepIsAccepted() throws Exception {
        TotpService.Enrollment enrollment = totpService.generateEnrollment("user@example.com");
        String code = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6)
                .generate(enrollment.rawSecret(), new SystemTimeProvider().getTime() / 30);

        assertThat(totpService.isValidCode(enrollment.encryptedSecret(), code)).isTrue();
    }

    @Test
    void wrongCodeIsRejected() {
        TotpService.Enrollment enrollment = totpService.generateEnrollment("user@example.com");

        assertThat(totpService.isValidCode(enrollment.encryptedSecret(), "000000")).isFalse();
    }

    @Test
    void nullOrBlankCodeIsRejected() {
        TotpService.Enrollment enrollment = totpService.generateEnrollment("user@example.com");

        assertThat(totpService.isValidCode(enrollment.encryptedSecret(), null)).isFalse();
        assertThat(totpService.isValidCode(enrollment.encryptedSecret(), "")).isFalse();
    }
}
