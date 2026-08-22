package com.mywiki.service.impl;

import com.mywiki.exception.*;
import com.mywiki.model.dto.FolderRequest;
import com.mywiki.model.dto.FolderResponse;
import com.mywiki.model.entity.Folder;
import com.mywiki.model.entity.Page;
import com.mywiki.model.entity.User;
import com.mywiki.model.entity.Workspace;
import com.mywiki.repository.FolderRepository;
import com.mywiki.repository.LinkRepository;
import com.mywiki.repository.PageRepository;
import com.mywiki.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FolderServiceImplTest {

    @Mock
    private FolderRepository folderRepository;
    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private PageRepository pageRepository;
    @Mock
    private LinkRepository linkRepository;

    private FolderServiceImpl folderService;
    private User owner;
    private Workspace workspace;

    @BeforeEach
    void setUp() {
        folderService = new FolderServiceImpl(
                folderRepository,
                workspaceRepository,
                pageRepository,
                linkRepository
        );
        owner = new User("Owner", "owner@mywiki.com", "hash");
        owner.setUserId(1);
        workspace = new Workspace(owner, "World", null, "Private");
        workspace.setWorkspaceId(10);
    }

    @Test
    void createFolder_shouldCreateRootFolder() {
        when(workspaceRepository.findById(10)).thenReturn(Optional.of(workspace));
        when(folderRepository.existsByWorkspaceWorkspaceIdAndNameIgnoreCaseAndParentFolderIsNull(
                10, "Characters"
        )).thenReturn(false);
        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> {
            Folder folder = invocation.getArgument(0);
            folder.setFolderId(20);
            return folder;
        });

        FolderResponse response = folderService.createFolder(1, 10, request("Characters", null, null));

        assertEquals(20, response.folderId());
        assertEquals("Characters", response.name());
        assertEquals(10, response.workspaceId());
    }

    @Test
    void createFolder_shouldRejectDuplicateSiblingName() {
        when(workspaceRepository.findById(10)).thenReturn(Optional.of(workspace));
        when(folderRepository.existsByWorkspaceWorkspaceIdAndNameIgnoreCaseAndParentFolderIsNull(
                10, "Characters"
        )).thenReturn(true);

        assertThrows(
                FolderNameAlreadyExistsException.class,
                () -> folderService.createFolder(1, 10, request("Characters", null, null))
        );
        verify(folderRepository, never()).save(any());
    }

    @Test
    void getFolder_shouldRejectPrivateWorkspaceForAnotherUser() {
        when(workspaceRepository.findById(10)).thenReturn(Optional.of(workspace));

        assertThrows(
                FolderAccessDeniedException.class,
                () -> folderService.getFolder(10, 20, 99)
        );
        verify(folderRepository, never()).findAllByWorkspaceWorkspaceId(any());
    }

    @Test
    void updateFolder_shouldRejectMovingIntoOwnDescendant() {
        Folder root = folder(20, null, "Root");
        Folder child = folder(21, root, "Child");
        when(workspaceRepository.findById(10)).thenReturn(Optional.of(workspace));
        when(folderRepository.findAllByWorkspaceWorkspaceId(10))
                .thenReturn(List.of(root, child));

        FolderRequest request = request("Root", null, 21);

        assertThrows(
                InvalidFolderMoveException.class,
                () -> folderService.updateFolder(1, 10, 20, request)
        );
    }

    @Test
    void deleteFolder_shouldDeleteSubtreePagesAndLinks() {
        Folder root = folder(20, null, "Root");
        Folder child = folder(21, root, "Child");
        Page page = new Page(workspace, child, "Page", "Content");
        page.setPageId(30);

        when(workspaceRepository.findById(10)).thenReturn(Optional.of(workspace));
        when(folderRepository.findAllByWorkspaceWorkspaceId(10))
                .thenReturn(List.of(root, child));
        when(pageRepository.findAll()).thenReturn(List.of(page));

        folderService.deleteFolder(1, 10, 20);

        verify(linkRepository).deleteAllByPageIds(anyCollection());
        verify(pageRepository).deleteAllByFolderIds(anyCollection());
        verify(folderRepository).deleteAll(anyList());
    }

    private Folder folder(int id, Folder parent, String name) {
        Folder folder = new Folder(workspace, parent, name, null);
        folder.setFolderId(id);
        return folder;
    }

    private FolderRequest request(String name, String description, Integer parentFolderId) {
        FolderRequest request = new FolderRequest();
        request.setName(name);
        request.setDescription(description);
        request.setParentFolderId(parentFolderId);
        return request;
    }
}
