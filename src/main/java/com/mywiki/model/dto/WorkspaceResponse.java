package com.mywiki.model.dto;

import com.mywiki.model.entity.Workspace;

import java.time.LocalDateTime;

public record WorkspaceResponse(
        Integer workspaceId,
        Integer ownerId,
        String ownerUsername,
        String name,
        String description,
        String visibility,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static WorkspaceResponse from(Workspace workspace) {
        return new WorkspaceResponse(
                workspace.getWorkspaceId(),
                workspace.getOwner().getUserId(),
                workspace.getOwner().getUsername(),
                workspace.getName(),
                workspace.getDescription(),
                workspace.getVisibility(),
                workspace.getCreatedAt(),
                workspace.getUpdatedAt()
        );
    }
}
