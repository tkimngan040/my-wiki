package com.mywiki.model.dto;

import java.util.List;

public record PaginatedLinkResponse(
        List<LinkResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String sort,
        String direction
) {
}
