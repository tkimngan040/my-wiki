package com.mywiki.service.impl;

import com.mywiki.exception.*;
import com.mywiki.model.dto.LinkRequest;
import com.mywiki.model.entity.*;
import com.mywiki.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LinkServiceImplTest {
    @Mock LinkRepository linkRepository;
    @Mock PageRepository pageRepository;
    @Mock WorkspaceRepository workspaceRepository;
    private LinkServiceImpl linkService;
    private User owner;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        linkService = new LinkServiceImpl(linkRepository, pageRepository, workspaceRepository);
        owner = new User("Owner", "owner@mywiki.com", "hash");
        owner.setUserId(1);
        workspace = new Workspace(owner, "World", null, "Private");
        workspace.setWorkspaceId(10);
    }

    @Test
    void createLink_shouldLinkPagesInSameWorkspace() {
        Page source = page(20, "Source");
        Page target = page(21, "Target");
        when(workspaceRepository.findById(10)).thenReturn(Optional.of(workspace));
        when(pageRepository.findAllByWorkspaceWorkspaceId(10)).thenReturn(List.of(source, target));
        when(linkRepository.save(any(Link.class))).thenAnswer(invocation -> {
            Link link = invocation.getArgument(0);
            link.setLinkId(30);
            return link;
        });
        var response = linkService.createLink(1, 10, 20, request(21, "See details"));
        assertEquals(30, response.linkId());
        assertEquals(21, response.targetPageId());
    }

    @Test
    void createLink_shouldRejectSelfLink() {
        Page page = page(20, "Page");
        when(workspaceRepository.findById(10)).thenReturn(Optional.of(workspace));
        when(pageRepository.findAllByWorkspaceWorkspaceId(10)).thenReturn(List.of(page));
        assertThrows(InvalidLinkException.class,
                () -> linkService.createLink(1, 10, 20, request(20, "Self")));
    }

    @Test
    void getLinks_shouldRejectPrivateWorkspaceForAnotherUser() {
        when(workspaceRepository.findById(10)).thenReturn(Optional.of(workspace));
        assertThrows(LinkAccessDeniedException.class, () -> linkService.getLinks(10, 20, 99));
    }

    private Page page(int id, String title) {
        Page page = new Page(workspace, null, title, "Text");
        page.setPageId(id);
        return page;
    }
    private LinkRequest request(int targetPageId, String anchorText) {
        LinkRequest request = new LinkRequest();
        request.setTargetPageId(targetPageId);
        request.setAnchorText(anchorText);
        return request;
    }
}
