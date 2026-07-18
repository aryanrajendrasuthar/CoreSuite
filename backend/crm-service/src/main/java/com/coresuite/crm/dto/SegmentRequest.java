package com.coresuite.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record SegmentRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 2000) String description,
        @NotEmpty Set<@Pattern(regexp = "^[a-z0-9_-]{1,64}$") String> requiredTags) {
}
