package com.mywiki.service.interfaces;

import com.mywiki.model.dto.PageRequest;
import com.mywiki.model.dto.PageResponse;
import java.util.List;

public interface PageService {
    List<PageResponse> getVisiblePages(Integer workspaceId, Integer userId);
    PageResponse getPage(Integer workspaceId, Integer pageId, Integer userId);
    PageResponse createPage(Integer userId, Integer workspaceId, PageRequest request);
    PageResponse updatePage(Integer userId, Integer workspaceId, Integer pageId, PageRequest request);
    void deletePage(Integer userId, Integer workspaceId, Integer pageId);
}
