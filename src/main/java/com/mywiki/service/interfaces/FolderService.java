package com.mywiki.service.interfaces;

import com.mywiki.model.dto.FolderRequest;
import com.mywiki.model.dto.FolderResponse;
import com.mywiki.model.dto.PaginatedFolderResponse;

public interface FolderService {
    default java.util.List<FolderResponse> getVisibleFolders(Integer workspaceId, Integer userId) {
        return getVisibleFolders(workspaceId, userId, 0, 20, "name", "asc").content();
    }
    PaginatedFolderResponse getVisibleFolders(Integer workspaceId, Integer userId, int page, int size, String sort, String direction);
    FolderResponse getFolder(Integer workspaceId, Integer folderId, Integer userId);
    FolderResponse createFolder(Integer userId, Integer workspaceId, FolderRequest request);
    FolderResponse updateFolder(Integer userId, Integer workspaceId, Integer folderId, FolderRequest request);
    void deleteFolder(Integer userId, Integer workspaceId, Integer folderId);
}
