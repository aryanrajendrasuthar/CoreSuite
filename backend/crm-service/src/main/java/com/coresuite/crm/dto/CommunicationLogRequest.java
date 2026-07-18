package com.coresuite.crm.dto;

import com.coresuite.crm.domain.CommunicationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CommunicationLogRequest(
        @NotNull CommunicationChannel channel,
        @NotBlank @Size(max = 255) String subject,
        @Size(max = 10000) String body,
        @NotNull Instant occurredAt) {
}
