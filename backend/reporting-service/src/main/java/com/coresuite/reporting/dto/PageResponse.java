package com.coresuite.reporting.dto;

import java.util.List;

/** Minimal view of a Spring Data {@code Page} JSON body, as returned by other services' list endpoints. */
public record PageResponse<T>(List<T> content, int totalPages, int number) {

    public boolean hasNext() {
        return number + 1 < totalPages;
    }
}
