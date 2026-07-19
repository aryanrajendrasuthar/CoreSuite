package com.coresuite.reporting.client;

import com.coresuite.reporting.dto.OrderSummary;
import com.coresuite.reporting.dto.PageResponse;
import com.coresuite.shared.error.DownstreamServiceException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class OrderServiceClient {

    private static final int PAGE_SIZE = 200;

    private final RestClient restClient;

    public OrderServiceClient(@Qualifier("orderServiceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /** Fetches every order by walking order-service's pagination — fine at this project's scale. */
    public List<OrderSummary> fetchAllOrders() {
        List<OrderSummary> orders = new ArrayList<>();
        int page = 0;
        PageResponse<OrderSummary> response;
        do {
            response = fetchPage(page);
            orders.addAll(response.content());
            page++;
        } while (response.hasNext());
        return orders;
    }

    private PageResponse<OrderSummary> fetchPage(int page) {
        try {
            return restClient.get()
                    .uri("/api/orders?page={page}&size={size}", page, PAGE_SIZE)
                    .retrieve()
                    .body(new ParameterizedTypeReference<PageResponse<OrderSummary>>() {
                    });
        } catch (RestClientException e) {
            throw new DownstreamServiceException("Failed to fetch orders from order-service", e);
        }
    }
}
