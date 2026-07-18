package com.coresuite.crm.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.coresuite.crm.AbstractIntegrationTest;
import com.coresuite.crm.domain.CommunicationChannel;
import com.coresuite.crm.dto.CommunicationLogRequest;
import com.coresuite.crm.dto.CustomerCreateRequest;
import com.coresuite.crm.dto.SegmentRequest;
import com.coresuite.crm.dto.TagRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class CustomerControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTagAndSegmentMatchLifecycle() throws Exception {
        long customerId = createCustomer("Ada Lovelace", "ada@example.com");

        mockMvc.perform(post("/api/customers/" + customerId + "/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TagRequest("vip"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0]").value("vip"));

        mockMvc.perform(post("/api/segments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SegmentRequest("VIP customers", "High value", Set.of("vip")))))
                .andExpect(status().isCreated());

        MvcResult segments = mockMvc.perform(get("/api/segments")).andExpect(status().isOk()).andReturn();
        long segmentId = objectMapper.readTree(segments.getResponse().getContentAsString())
                .get(0).get("id").asLong();

        mockMvc.perform(get("/api/segments/" + segmentId + "/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ada@example.com"));
    }

    @Test
    void duplicateEmailReturnsConflict() throws Exception {
        createCustomer("First", "dup@example.com");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CustomerCreateRequest("Second", "dup@example.com", null, null))))
                .andExpect(status().isConflict());
    }

    @Test
    void loggingCommunicationForUnknownCustomerReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/customers/999999/communications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommunicationLogRequest(
                                CommunicationChannel.EMAIL, "Hi", "body", Instant.now()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void logAndFetchCommunicationHistory() throws Exception {
        long customerId = createCustomer("Grace Hopper", "grace@example.com");

        mockMvc.perform(post("/api/customers/" + customerId + "/communications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommunicationLogRequest(
                                CommunicationChannel.CALL, "Intro call", "Discussed onboarding", Instant.now()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject").value("Intro call"));

        mockMvc.perform(get("/api/customers/" + customerId + "/communications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].channel").value("CALL"));
    }

    @Test
    void deletingCustomerRemovesThem() throws Exception {
        long customerId = createCustomer("Temp", "temp@example.com");

        mockMvc.perform(delete("/api/customers/" + customerId)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/customers/" + customerId)).andExpect(status().isNotFound());
    }

    private long createCustomer(String name, String email) throws Exception {
        MvcResult created = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CustomerCreateRequest(name, email, null, null))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
    }
}
