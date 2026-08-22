package com.mywiki.service.interfaces;

import com.mywiki.model.dto.FolderRequest;
import com.mywiki.model.dto.FolderResponse;

import java.util.List;

public interface FolderService {
    List<FolderResponse> getVisibleFolders(Integer workspaceId, Integer userId);
    FolderResponse getFolder(Integer workspaceId, Integer folderId, Integer userId);
    FolderResponse createFolder(Integer userId, Integer workspaceId, FolderRequest request);
    FolderResponse updateFolder(Integer userId, Integer workspaceId, Integer folderId, FolderRequest request);
    void deleteFolder(Integer userId, Integer workspaceId, Integer folderId);
}
