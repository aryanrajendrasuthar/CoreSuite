package com.coresuite.product.dto;

import com.coresuite.product.domain.ProductVariant;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record VariantResponse(
        Long id,
        Long productId,
        String sku,
        Map<String, String> attributes,
        BigDecimal price,
        String currency,
        Instant createdAt,
        Instant updatedAt) {

    public static VariantResponse from(ProductVariant variant) {
        return new VariantResponse(
                variant.getId(),
                variant.getProduct().getId(),
                variant.getSku(),
                variant.getAttributes(),
                variant.getPrice(),
                variant.getCurrency(),
                variant.getCreatedAt(),
                variant.getUpdatedAt());
    }
}
