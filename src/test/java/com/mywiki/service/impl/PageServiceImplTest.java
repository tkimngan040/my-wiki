package com.mywiki.service.impl;

import com.mywiki.exception.*;
import com.mywiki.model.dto.PageRequest;
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
class PageServiceImplTest {
    @Mock PageRepository pageRepository;
    @Mock WorkspaceRepository workspaceRepository;
    @Mock FolderRepository folderRepository;
    @Mock LinkRepository linkRepository;
    private PageServiceImpl pageService;
    private User owner;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        pageService = new PageServiceImpl(pageRepository, workspaceRepository, folderRepository, linkRepository);
        owner = new User("Owner", "owner@mywiki.com", "hash");
        owner.setUserId(1);
        workspace = new Workspace(owner, "World", null, "Private");
        workspace.setWorkspaceId(10);
    }

    @Test
    void createPage_shouldCreatePage() {
        when(workspaceRepository.findById(10)).thenReturn(Optional.of(workspace));
        when(pageRepository.existsByWorkspaceWorkspaceIdAndTitleIgnoreCase(10, "Overview")).thenReturn(false);
        when(pageRepository.save(any(Page.class))).thenAnswer(invocation -> {
            Page page = invocation.getArgument(0);
            page.setPageId(20);
            return page;
        });
        var response = pageService.createPage(1, 10, request("Overview", "Text", null));
        assertEquals(20, response.pageId());
        assertEquals("Overview", response.title());
    }

    @Test
    void createPage_shouldRejectDuplicateTitle() {
        when(workspaceRepository.findById(10)).thenReturn(Optional.of(workspace));
        when(pageRepository.existsByWorkspaceWorkspaceIdAndTitleIgnoreCase(10, "Overview")).thenReturn(true);
        assertThrows(PageNameAlreadyExistsException.class,
                () -> pageService.createPage(1, 10, request("Overview", null, null)));
    }

    @Test
    void getPage_shouldRejectPrivateWorkspaceForAnotherUser() {
        when(workspaceRepository.findById(10)).thenReturn(Optional.of(workspace));
        assertThrows(PageAccessDeniedException.class, () -> pageService.getPage(10, 20, 99));
    }

    @Test
    void deletePage_shouldDeleteLinksBeforePage() {
        Page page = new Page(workspace, null, "Overview", "Text");
        page.setPageId(20);
        when(workspaceRepository.findById(10)).thenReturn(Optional.of(workspace));
        when(pageRepository.findAllByWorkspaceWorkspaceId(10)).thenReturn(List.of(page));
        pageService.deletePage(1, 10, 20);
        verify(linkRepository).deleteAllByPageIds(List.of(20));
        verify(pageRepository).delete(page);
    }

    private PageRequest request(String title, String content, Integer folderId) {
        PageRequest request = new PageRequest();
        request.setTitle(title);
        request.setContent(content);
        request.setFolderId(folderId);
        return request;
    }
}
