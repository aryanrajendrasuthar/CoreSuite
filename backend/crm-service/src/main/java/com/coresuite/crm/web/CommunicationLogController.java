package com.coresuite.crm.web;

import com.coresuite.crm.dto.CommunicationLogRequest;
import com.coresuite.crm.dto.CommunicationLogResponse;
import com.coresuite.crm.service.CommunicationLogService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers/{customerId}/communications")
@RequiredArgsConstructor
public class CommunicationLogController {

    private final CommunicationLogService communicationLogService;

    @PostMapping
    public ResponseEntity<CommunicationLogResponse> log(
            @PathVariable Long customerId, @Valid @RequestBody CommunicationLogRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(communicationLogService.log(customerId, request));
    }

    @GetMapping
    public List<CommunicationLogResponse> history(@PathVariable Long customerId) {
        return communicationLogService.history(customerId);
    }
}
