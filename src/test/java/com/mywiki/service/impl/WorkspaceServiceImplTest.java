package com.mywiki.service.impl;

import com.mywiki.exception.WorkspaceAccessDeniedException;
import com.mywiki.exception.WorkspaceNameAlreadyExistsException;
import com.mywiki.model.dto.WorkspaceRequest;
import com.mywiki.model.dto.WorkspaceResponse;
import com.mywiki.model.entity.User;
import com.mywiki.model.entity.Workspace;
import com.mywiki.repository.UserRepository;
import com.mywiki.repository.WorkspaceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class WorkspaceServiceImplTest {

    @Autowired
    private WorkspaceServiceImpl workspaceService;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Workspace testWorkspace;

    @AfterEach
    void cleanUp() {
        if (testWorkspace != null
                && workspaceRepository.existsById(testWorkspace.getWorkspaceId())) {
            workspaceRepository.delete(testWorkspace);
        }
        if (testUser != null
                && userRepository.existsById(testUser.getUserId())) {
            userRepository.delete(testUser);
        }
    }

    @Test
    void createWorkspace_shouldCreateWorkspaceForOwner() {
        testUser = saveUser();

        WorkspaceResponse response = workspaceService.createWorkspace(
                testUser.getUserId(),
                request("World", "A novel world", "private")
        );
        testWorkspace = workspaceRepository.findById(response.workspaceId()).orElseThrow();

        assertEquals("World", response.name());
        assertEquals("Private", response.visibility());
        assertEquals(testUser.getUserId(), response.ownerId());
    }

    @Test
    void createWorkspace_shouldRejectDuplicateNameForSameOwner() {
        testUser = saveUser();
        testWorkspace = workspaceRepository.save(
                new Workspace(testUser, "World", null, "Private")
        );

        assertThrows(
                WorkspaceNameAlreadyExistsException.class,
                () -> workspaceService.createWorkspace(
                        testUser.getUserId(),
                        request("World", null, "Public")
                )
        );
    }

    @Test
    void getWorkspace_shouldRejectPrivateWorkspaceForAnotherUser() {
        testUser = saveUser();
        testWorkspace = workspaceRepository.save(
                new Workspace(testUser, "Private World", null, "Private")
        );

        assertThrows(
                WorkspaceAccessDeniedException.class,
                () -> workspaceService.getWorkspace(testWorkspace.getWorkspaceId(), -1)
        );
    }

    @Test
    void updateWorkspace_shouldUpdateWorkspaceForOwner() {
        testUser = saveUser();
        testWorkspace = workspaceRepository.save(
                new Workspace(testUser, "Old Name", null, "Private")
        );

        WorkspaceResponse response = workspaceService.updateWorkspace(
                testUser.getUserId(),
                testWorkspace.getWorkspaceId(),
                request("New Name", "Updated description", "Public")
        );

        assertEquals("New Name", response.name());
        assertEquals("Updated description", response.description());
        assertEquals("Public", response.visibility());
    }

    @Test
    void deleteWorkspace_shouldDeleteWorkspaceForOwner() {
        testUser = saveUser();
        testWorkspace = workspaceRepository.save(
                new Workspace(testUser, "World", null, "Private")
        );
        Integer workspaceId = testWorkspace.getWorkspaceId();

        workspaceService.deleteWorkspace(testUser.getUserId(), workspaceId);
        testWorkspace = null;

        assertEquals(false, workspaceRepository.existsById(workspaceId));
    }

    private User saveUser() {
        return userRepository.save(
                new User(
                        "Workspace Test User",
                        "workspace-" + UUID.randomUUID() + "@mywiki.com",
                        "test-password-hash"
                )
        );
    }

    private WorkspaceRequest request(
            String name,
            String description,
            String visibility
    ) {
        WorkspaceRequest request = new WorkspaceRequest();
        request.setName(name);
        request.setDescription(description);
        request.setVisibility(visibility);
        return request;
    }
}
