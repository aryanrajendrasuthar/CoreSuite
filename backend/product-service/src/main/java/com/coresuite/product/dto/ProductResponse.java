package com.coresuite.product.dto;

import com.coresuite.product.domain.Product;
import com.coresuite.product.domain.ProductStatus;
import java.time.Instant;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String description,
        String category,
        ProductStatus status,
        List<VariantResponse> variants,
        Instant createdAt,
        Instant updatedAt) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getCategory(),
                product.getStatus(),
                product.getVariants().stream().map(VariantResponse::from).toList(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
