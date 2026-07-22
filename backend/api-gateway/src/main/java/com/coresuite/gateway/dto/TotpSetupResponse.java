package com.coresuite.gateway.dto;

public record TotpSetupResponse(String secret, String otpAuthUri) {
}
