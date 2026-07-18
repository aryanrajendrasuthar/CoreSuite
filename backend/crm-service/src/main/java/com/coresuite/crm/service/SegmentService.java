package com.coresuite.crm.service;

import com.coresuite.crm.domain.Segment;
import com.coresuite.crm.dto.SegmentRequest;
import com.coresuite.crm.dto.SegmentResponse;
import com.coresuite.crm.repository.SegmentRepository;
import com.coresuite.shared.error.ConflictException;
import com.coresuite.shared.error.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SegmentService {

    private final SegmentRepository segmentRepository;

    @Transactional
    public SegmentResponse create(SegmentRequest request) {
        if (segmentRepository.existsByName(request.name())) {
            throw new ConflictException("Segment already exists: " + request.name());
        }
        Segment segment = new Segment();
        segment.setName(request.name());
        segment.setDescription(request.description());
        segment.setRequiredTags(request.requiredTags());
        return SegmentResponse.from(segmentRepository.save(segment));
    }

    @Transactional(readOnly = true)
    public List<SegmentResponse> list() {
        return segmentRepository.findAll().stream().map(SegmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public SegmentResponse get(Long id) {
        return SegmentResponse.from(segmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Segment not found: " + id)));
    }
}
