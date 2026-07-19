package com.coresuite.reporting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "coresuite.services")
public record ServiceUrlsProperties(String orderServiceUrl, String inventoryServiceUrl) {
}
