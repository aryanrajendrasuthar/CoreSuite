package com.coresuite.crm.dto;

import com.coresuite.crm.domain.CommunicationChannel;
import com.coresuite.crm.domain.CommunicationLogEntry;
import java.time.Instant;

public record CommunicationLogResponse(
        String id,
        Long customerId,
        CommunicationChannel channel,
        String subject,
        String body,
        Instant occurredAt,
        Instant createdAt) {

    public static CommunicationLogResponse from(CommunicationLogEntry entry) {
        return new CommunicationLogResponse(
                entry.getId(),
                entry.getCustomerId(),
                entry.getChannel(),
                entry.getSubject(),
                entry.getBody(),
                entry.getOccurredAt(),
                entry.getCreatedAt());
    }
}
