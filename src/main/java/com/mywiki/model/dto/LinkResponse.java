package com.mywiki.model.dto;

import com.mywiki.model.entity.Link;
import java.time.LocalDateTime;

public record LinkResponse(
        Integer linkId, Integer workspaceId, Integer sourcePageId, String sourcePageTitle,
        Integer targetPageId, String targetPageTitle, String anchorText, LocalDateTime createdAt
) {
    public static LinkResponse from(Link link) {
        return new LinkResponse(
                link.getLinkId(), link.getWorkspace().getWorkspaceId(),
                link.getSourcePage().getPageId(), link.getSourcePage().getTitle(),
                link.getTargetPage().getPageId(), link.getTargetPage().getTitle(),
                link.getAnchorText(), link.getCreatedAt()
        );
    }
}
