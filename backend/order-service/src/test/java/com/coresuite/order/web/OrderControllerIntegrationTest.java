package com.coresuite.order.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coresuite.order.AbstractIntegrationTest;
import com.coresuite.order.domain.OrderStatus;
import com.coresuite.order.dto.OrderCreateRequest;
import com.coresuite.order.dto.OrderLineItemRequest;
import com.coresuite.order.dto.OrderStatusUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class OrderControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createOrderAndWalkStatusLifecycle() throws Exception {
        long orderId = createOrder();

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrderStatusUpdateRequest(OrderStatus.CONFIRMED, "paid"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrderStatusUpdateRequest(OrderStatus.PROCESSING, null))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders/" + orderId + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].toStatus").value("PENDING"))
                .andExpect(jsonPath("$[2].toStatus").value("PROCESSING"));
    }

    @Test
    void invalidStatusTransitionReturnsConflict() throws Exception {
        long orderId = createOrder();

        mockMvc.perform(patch("/api/orders/" + orderId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrderStatusUpdateRequest(OrderStatus.DELIVERED, null))))
                .andExpect(status().isConflict());
    }

    @Test
    void creatingOrderWithNoLineItemsReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new OrderCreateRequest(1L, null, List.of()))))
                .andExpect(status().isBadRequest());
    }

    private long createOrder() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderCreateRequest(
                                1L,
                                null,
                                List.of(new OrderLineItemRequest("SKU-1", 2, new BigDecimal("15.00")))))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(30.00))
                .andReturn();
        return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
    }
}
