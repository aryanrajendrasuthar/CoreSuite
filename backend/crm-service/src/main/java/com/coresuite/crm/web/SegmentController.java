package com.coresuite.crm.web;

import com.coresuite.crm.dto.CustomerResponse;
import com.coresuite.crm.dto.SegmentRequest;
import com.coresuite.crm.dto.SegmentResponse;
import com.coresuite.crm.service.CustomerService;
import com.coresuite.crm.service.SegmentService;
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
@RequestMapping("/api/segments")
@RequiredArgsConstructor
public class SegmentController {

    private final SegmentService segmentService;
    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<SegmentResponse> create(@Valid @RequestBody SegmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(segmentService.create(request));
    }

    @GetMapping
    public List<SegmentResponse> list() {
        return segmentService.list();
    }

    @GetMapping("/{id}")
    public SegmentResponse get(@PathVariable Long id) {
        return segmentService.get(id);
    }

    @GetMapping("/{id}/customers")
    public List<CustomerResponse> customers(@PathVariable Long id) {
        return customerService.customersInSegment(id);
    }
}
