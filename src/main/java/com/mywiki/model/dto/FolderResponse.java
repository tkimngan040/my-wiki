package com.mywiki.model.dto;

import com.mywiki.model.entity.Folder;

import java.time.LocalDateTime;

public record FolderResponse(
        Integer folderId,
        Integer workspaceId,
        Integer parentFolderId,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FolderResponse from(Folder folder) {
        return new FolderResponse(
                folder.getFolderId(),
                folder.getWorkspace().getWorkspaceId(),
                folder.getParentFolder() == null ? null : folder.getParentFolder().getFolderId(),
                folder.getName(),
                folder.getDescription(),
                folder.getCreatedAt(),
                folder.getUpdatedAt()
        );
    }
}
