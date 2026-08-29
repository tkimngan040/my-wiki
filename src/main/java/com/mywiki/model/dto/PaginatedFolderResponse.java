package com.mywiki.model.dto;

import java.util.List;

public record PaginatedFolderResponse(
        List<FolderResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        String sort,
        String direction
) {
}
