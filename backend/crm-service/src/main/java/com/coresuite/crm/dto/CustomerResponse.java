package com.coresuite.crm.dto;

import com.coresuite.crm.domain.Customer;
import java.time.Instant;
import java.util.Set;

public record CustomerResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String company,
        Set<String> tags,
        Instant createdAt,
        Instant updatedAt) {

    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCompany(),
                customer.getTags(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }
}
