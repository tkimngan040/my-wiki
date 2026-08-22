package com.mywiki.service.interfaces;

import com.mywiki.model.dto.LinkRequest;
import com.mywiki.model.dto.LinkResponse;
import java.util.List;

public interface LinkService {
    List<LinkResponse> getLinks(Integer workspaceId, Integer sourcePageId, Integer userId);
    LinkResponse createLink(Integer userId, Integer workspaceId, Integer sourcePageId, LinkRequest request);
    void deleteLink(Integer userId, Integer workspaceId, Integer sourcePageId, Integer linkId);
}
