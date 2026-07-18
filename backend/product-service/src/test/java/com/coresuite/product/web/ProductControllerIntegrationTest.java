package com.coresuite.product.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coresuite.product.AbstractIntegrationTest;
import com.coresuite.product.domain.ProductStatus;
import com.coresuite.product.dto.ProductCreateRequest;
import com.coresuite.product.dto.ProductUpdateRequest;
import com.coresuite.product.dto.VariantRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class ProductControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createGetUpdateAndDeleteProductLifecycle() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProductCreateRequest("Widget", "A widget", "Hardware"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Widget"))
                .andReturn();

        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Hardware"));

        mockMvc.perform(put("/api/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProductUpdateRequest("Widget Pro", "Updated", "Hardware", ProductStatus.DISCONTINUED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Widget Pro"))
                .andExpect(jsonPath("$.status").value("DISCONTINUED"));

        mockMvc.perform(delete("/api/products/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/products/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void addingVariantWithDuplicateSkuReturnsConflict() throws Exception {
        long productId = createProduct("Gadget");

        mockMvc.perform(post("/api/products/" + productId + "/variants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VariantRequest("SKU-DUP-1", null, new BigDecimal("19.99"), "USD"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/products/" + productId + "/variants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new VariantRequest("SKU-DUP-1", null, new BigDecimal("29.99"), "USD"))))
                .andExpect(status().isConflict());
    }

    @Test
    void creatingProductWithBlankNameReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ProductCreateRequest("", null, null))))
                .andExpect(status().isBadRequest());
    }

    private long createProduct(String name) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProductCreateRequest(name, null, null))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
    }
}
