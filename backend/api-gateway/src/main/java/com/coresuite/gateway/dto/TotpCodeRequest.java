package com.coresuite.gateway.dto;

import jakarta.validation.constraints.NotBlank;

public record TotpCodeRequest(@NotBlank String code) {
}
