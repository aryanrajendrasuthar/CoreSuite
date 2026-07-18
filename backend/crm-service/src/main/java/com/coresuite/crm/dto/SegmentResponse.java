package com.coresuite.crm.dto;

import com.coresuite.crm.domain.Segment;
import java.time.Instant;
import java.util.Set;

public record SegmentResponse(
        Long id, String name, String description, Set<String> requiredTags, Instant createdAt, Instant updatedAt) {

    public static SegmentResponse from(Segment segment) {
        return new SegmentResponse(
                segment.getId(),
                segment.getName(),
                segment.getDescription(),
                segment.getRequiredTags(),
                segment.getCreatedAt(),
                segment.getUpdatedAt());
    }
}
