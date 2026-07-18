package com.coresuite.crm.repository;

import com.coresuite.crm.domain.CommunicationLogEntry;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommunicationLogRepository extends MongoRepository<CommunicationLogEntry, String> {

    List<CommunicationLogEntry> findByCustomerIdOrderByOccurredAtDesc(Long customerId);
}
