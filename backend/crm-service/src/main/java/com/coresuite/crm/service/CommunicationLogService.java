package com.coresuite.crm.service;

import com.coresuite.crm.domain.CommunicationLogEntry;
import com.coresuite.crm.dto.CommunicationLogRequest;
import com.coresuite.crm.dto.CommunicationLogResponse;
import com.coresuite.crm.repository.CommunicationLogRepository;
import com.coresuite.crm.repository.CustomerRepository;
import com.coresuite.shared.error.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunicationLogService {

    private final CommunicationLogRepository communicationLogRepository;
    private final CustomerRepository customerRepository;

    public CommunicationLogResponse log(Long customerId, CommunicationLogRequest request) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }
        CommunicationLogEntry entry = new CommunicationLogEntry();
        entry.setCustomerId(customerId);
        entry.setChannel(request.channel());
        entry.setSubject(request.subject());
        entry.setBody(request.body());
        entry.setOccurredAt(request.occurredAt());
        entry.setCreatedAt(Instant.now());
        return CommunicationLogResponse.from(communicationLogRepository.save(entry));
    }

    public List<CommunicationLogResponse> history(Long customerId) {
        return communicationLogRepository.findByCustomerIdOrderByOccurredAtDesc(customerId).stream()
                .map(CommunicationLogResponse::from)
                .toList();
    }
}
