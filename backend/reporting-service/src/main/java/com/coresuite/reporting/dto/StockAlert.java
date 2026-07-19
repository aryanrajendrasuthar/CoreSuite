package com.coresuite.reporting.dto;

/** Minimal read-only view of a low-stock alert, as returned by inventory-service's API. */
public record StockAlert(String sku, int quantity, int reorderThreshold) {
}
