package com.mywiki.service.interfaces;

import com.mywiki.model.dto.PaginatedWorkspaceResponse;
import com.mywiki.model.dto.WorkspaceRequest;
import com.mywiki.model.dto.WorkspaceResponse;

public interface WorkspaceService {
    PaginatedWorkspaceResponse getVisibleWorkspaces(Integer userId, int page, int size, String sort, String direction);
    WorkspaceResponse getWorkspace(Integer workspaceId, Integer userId);
    WorkspaceResponse createWorkspace(Integer userId, WorkspaceRequest request);
    WorkspaceResponse updateWorkspace(Integer userId, Integer workspaceId, WorkspaceRequest request);
    void deleteWorkspace(Integer userId, Integer workspaceId);
}
