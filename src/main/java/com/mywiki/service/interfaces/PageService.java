package com.mywiki.service.interfaces;

import com.mywiki.model.dto.PageRequest;
import com.mywiki.model.dto.PageResponse;
import com.mywiki.model.dto.PaginatedPageResponse;

public interface PageService {
    PaginatedPageResponse getVisiblePages(Integer workspaceId, Integer userId, int page, int size, String sort, String direction);
    PageResponse getPage(Integer workspaceId, Integer pageId, Integer userId);
    PageResponse createPage(Integer userId, Integer workspaceId, PageRequest request);
    PageResponse updatePage(Integer userId, Integer workspaceId, Integer pageId, PageRequest request);
    void deletePage(Integer userId, Integer workspaceId, Integer pageId);
}
