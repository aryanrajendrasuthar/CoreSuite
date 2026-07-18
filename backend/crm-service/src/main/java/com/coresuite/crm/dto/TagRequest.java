package com.coresuite.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TagRequest(@NotBlank @Pattern(regexp = "^[a-z0-9_-]{1,64}$") String tag) {
}
