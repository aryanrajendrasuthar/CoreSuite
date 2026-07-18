package com.coresuite.crm.domain;

import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "communication_log")
@Getter
@Setter
@NoArgsConstructor
public class CommunicationLogEntry {

    @Id
    private String id;

    private Long customerId;

    private CommunicationChannel channel;

    private String subject;

    private String body;

    private Instant occurredAt;

    private Instant createdAt;
}
