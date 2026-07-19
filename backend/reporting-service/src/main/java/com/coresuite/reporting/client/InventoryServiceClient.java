package com.coresuite.reporting.client;

import com.coresuite.reporting.dto.StockAlert;
import com.coresuite.shared.error.DownstreamServiceException;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class InventoryServiceClient {

    private final RestClient restClient;

    public InventoryServiceClient(@Qualifier("inventoryServiceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public List<StockAlert> fetchReorderAlerts() {
        try {
            return restClient.get()
                    .uri("/api/stock/reorder-alerts")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<StockAlert>>() {
                    });
        } catch (RestClientException e) {
            throw new DownstreamServiceException("Failed to fetch reorder alerts from inventory-service", e);
        }
    }
}
