package com.mywiki.model.dto;

import java.util.List;

public record PaginatedPageResponse(
        List<PageResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String sort,
        String direction
) {
}
