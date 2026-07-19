package com.coresuite.reporting.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.coresuite.reporting.dto.StockAlert;
import com.coresuite.shared.error.DownstreamServiceException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class InventoryServiceClientTest {

    @Test
    void fetchReorderAlertsReturnsParsedList() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        InventoryServiceClient client = new InventoryServiceClient(builder.build());

        server.expect(requestTo("/api/stock/reorder-alerts"))
                .andRespond(withSuccess(
                        """
                        [{"sku":"SKU-1","quantity":2,"reorderThreshold":5}]
                        """,
                        MediaType.APPLICATION_JSON));

        List<StockAlert> alerts = client.fetchReorderAlerts();

        assertThat(alerts).hasSize(1);
        assertThat(alerts.get(0).sku()).isEqualTo("SKU-1");
        server.verify();
    }

    @Test
    void wrapsFailuresAsDownstreamServiceException() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        InventoryServiceClient client = new InventoryServiceClient(builder.build());

        server.expect(requestTo("/api/stock/reorder-alerts")).andRespond(withServerError());

        assertThatThrownBy(client::fetchReorderAlerts).isInstanceOf(DownstreamServiceException.class);
    }
}
