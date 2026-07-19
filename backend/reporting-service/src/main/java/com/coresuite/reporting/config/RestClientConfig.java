package com.coresuite.reporting.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient orderServiceRestClient(RestClient.Builder builder, ServiceUrlsProperties properties) {
        return builder.baseUrl(properties.orderServiceUrl()).build();
    }

    @Bean
    public RestClient inventoryServiceRestClient(RestClient.Builder builder, ServiceUrlsProperties properties) {
        return builder.baseUrl(properties.inventoryServiceUrl()).build();
    }
}
