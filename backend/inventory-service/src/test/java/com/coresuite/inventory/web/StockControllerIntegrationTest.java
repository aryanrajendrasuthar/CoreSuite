package com.coresuite.inventory.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coresuite.inventory.AbstractIntegrationTest;
import com.coresuite.inventory.dto.StockAdjustRequest;
import com.coresuite.inventory.dto.StockLevelCreateRequest;
import com.coresuite.inventory.dto.WarehouseRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class StockControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void initializeAdjustAndAlertOnLowStock() throws Exception {
        long warehouseId = createWarehouse("Main Warehouse");

        MvcResult stockCreated = mockMvc.perform(post("/api/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new StockLevelCreateRequest(warehouseId, "SKU-100", 10, 5))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.belowReorderThreshold").value(false))
                .andReturn();

        long stockId = objectMapper.readTree(stockCreated.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(patch("/api/stock/" + stockId + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new StockAdjustRequest(-8))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.belowReorderThreshold").value(true));

        mockMvc.perform(get("/api/stock/reorder-alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sku").value("SKU-100"));
    }

    @Test
    void adjustingBelowZeroReturnsConflict() throws Exception {
        long warehouseId = createWarehouse("Overdraw Warehouse");
        MvcResult stockCreated = mockMvc.perform(post("/api/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new StockLevelCreateRequest(warehouseId, "SKU-200", 3, 1))))
                .andExpect(status().isCreated())
                .andReturn();
        long stockId = objectMapper.readTree(stockCreated.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(patch("/api/stock/" + stockId + "/adjust")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new StockAdjustRequest(-5))))
                .andExpect(status().isConflict());
    }

    private long createWarehouse(String name) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/warehouses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WarehouseRequest(name, null))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
    }
}
