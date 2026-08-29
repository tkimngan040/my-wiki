package com.mywiki.service.interfaces;

import com.mywiki.model.dto.LinkRequest;
import com.mywiki.model.dto.LinkResponse;
import com.mywiki.model.dto.PaginatedLinkResponse;

public interface LinkService {
    default java.util.List<LinkResponse> getLinks(Integer workspaceId, Integer sourcePageId, Integer userId) {
        return getLinks(workspaceId, sourcePageId, userId, 0, 20, "createdAt", "desc").content();
    }
    PaginatedLinkResponse getLinks(Integer workspaceId, Integer sourcePageId, Integer userId, int page, int size, String sort, String direction);
    LinkResponse createLink(Integer userId, Integer workspaceId, Integer sourcePageId, LinkRequest request);
    void deleteLink(Integer userId, Integer workspaceId, Integer sourcePageId, Integer linkId);
}
