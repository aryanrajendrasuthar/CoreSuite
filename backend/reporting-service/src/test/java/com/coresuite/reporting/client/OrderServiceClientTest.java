package com.coresuite.reporting.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.coresuite.reporting.dto.OrderSummary;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class OrderServiceClientTest {

    @Test
    void fetchAllOrdersWalksEveryPage() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OrderServiceClient client = new OrderServiceClient(builder.build());

        server.expect(requestTo("/api/orders?page=0&size=200"))
                .andRespond(withSuccess(
                        """
                        {"content":[{"id":1,"customerId":1,"status":"PENDING","currency":"USD","totalAmount":10.00,"createdAt":"2026-01-01T00:00:00Z"}],
                         "totalPages":2,"number":0}
                        """,
                        MediaType.APPLICATION_JSON));
        server.expect(requestTo("/api/orders?page=1&size=200"))
                .andRespond(withSuccess(
                        """
                        {"content":[{"id":2,"customerId":1,"status":"DELIVERED","currency":"USD","totalAmount":20.00,"createdAt":"2026-01-02T00:00:00Z"}],
                         "totalPages":2,"number":1}
                        """,
                        MediaType.APPLICATION_JSON));

        List<OrderSummary> orders = client.fetchAllOrders();

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).id()).isEqualTo(1L);
        assertThat(orders.get(1).id()).isEqualTo(2L);
        server.verify();
    }
}
