package com.mywiki.service.interfaces;

import com.mywiki.model.dto.WorkspaceRequest;
import com.mywiki.model.dto.WorkspaceResponse;

import java.util.List;

public interface WorkspaceService {
    List<WorkspaceResponse> getVisibleWorkspaces(Integer userId);
    WorkspaceResponse getWorkspace(Integer workspaceId, Integer userId);
    WorkspaceResponse createWorkspace(Integer userId, WorkspaceRequest request);
    WorkspaceResponse updateWorkspace(Integer userId, Integer workspaceId, WorkspaceRequest request);
    void deleteWorkspace(Integer userId, Integer workspaceId);
}
