package com.mywiki.model.dto;

import com.mywiki.model.entity.Page;
import java.time.LocalDateTime;

public record PageResponse(
        Integer pageId, Integer workspaceId, Integer folderId, String title,
        String content, LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public static PageResponse from(Page page) {
        return new PageResponse(
                page.getPageId(), page.getWorkspace().getWorkspaceId(),
                page.getFolder() == null ? null : page.getFolder().getFolderId(),
                page.getTitle(), page.getContent(), page.getCreatedAt(), page.getUpdatedAt()
        );
    }
}
