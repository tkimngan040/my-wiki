package com.mywiki.controller;

import com.mywiki.model.dto.FolderResponse;
import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import com.mywiki.service.interfaces.FolderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FolderControllerTest {

    private FolderService folderService;
    private UserRepository userRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        folderService = mock(FolderService.class);
        userRepository = mock(UserRepository.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new FolderController(folderService, userRepository))
                .build();
    }

    @Test
    void getFolders_shouldReturnFoldersForAuthenticatedUser() throws Exception {
        User user = user("user@mywiki.com", 1);
        when(userRepository.findByEmail("user@mywiki.com")).thenReturn(Optional.of(user));
        when(folderService.getVisibleFolders(10, 1))
                .thenReturn(List.of(response(20, 10, null, "Characters")));

        mockMvc.perform(get("/api/workspaces/10/folders")
                        .principal(authentication("user@mywiki.com")))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {
                            "folderId": 20,
                            "workspaceId": 10,
                            "parentFolderId": null,
                            "name": "Characters"
                          }
                        ]
                        """));

        verify(folderService).getVisibleFolders(10, 1);
    }

    @Test
    void getFolder_shouldAllowAnonymousRequest() throws Exception {
        when(folderService.getFolder(10, 20, null))
                .thenReturn(response(20, 10, null, "Characters"));

        mockMvc.perform(get("/api/workspaces/10/folders/20"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "folderId": 20,
                          "workspaceId": 10,
                          "parentFolderId": null,
                          "name": "Characters"
                        }
                        """));

        verify(folderService).getFolder(10, 20, null);
    }

    @Test
    void createFolder_shouldReturnCreated() throws Exception {
        when(userRepository.findByEmail("user@mywiki.com"))
                .thenReturn(Optional.of(user("user@mywiki.com", 1)));
        when(folderService.createFolder(eq(1), eq(10), any()))
                .thenReturn(response(20, 10, null, "Characters"));

        mockMvc.perform(post("/api/workspaces/10/folders")
                        .principal(authentication("user@mywiki.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Characters",
                                  "description": "Character notes"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                          "folderId": 20,
                          "workspaceId": 10,
                          "name": "Characters"
                        }
                        """));

        verify(folderService).createFolder(eq(1), eq(10), any());
    }

    @Test
    void updateFolder_shouldDelegateToService() throws Exception {
        when(userRepository.findByEmail("user@mywiki.com"))
                .thenReturn(Optional.of(user("user@mywiki.com", 1)));
        when(folderService.updateFolder(eq(1), eq(10), eq(20), any()))
                .thenReturn(response(20, 10, 30, "Updated Characters"));

        mockMvc.perform(put("/api/workspaces/10/folders/20")
                        .principal(authentication("user@mywiki.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Characters",
                                  "description": "Updated",
                                  "parentFolderId": 30
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "folderId": 20,
                          "parentFolderId": 30,
                          "name": "Updated Characters"
                        }
                        """));

        verify(folderService).updateFolder(eq(1), eq(10), eq(20), any());
    }

    @Test
    void deleteFolder_shouldReturnNoContent() throws Exception {
        when(userRepository.findByEmail("user@mywiki.com"))
                .thenReturn(Optional.of(user("user@mywiki.com", 1)));

        mockMvc.perform(delete("/api/workspaces/10/folders/20")
                        .principal(authentication("user@mywiki.com")))
                .andExpect(status().isNoContent());

        verify(folderService).deleteFolder(1, 10, 20);
    }

    private UsernamePasswordAuthenticationToken authentication(String email) {
        return new UsernamePasswordAuthenticationToken(email, null);
    }

    private User user(String email, int id) {
        User user = new User("Test User", email, "hash");
        user.setUserId(id);
        return user;
    }

    private FolderResponse response(
            int folderId, int workspaceId, Integer parentFolderId, String name
    ) {
        return new FolderResponse(
                folderId,
                workspaceId,
                parentFolderId,
                name,
                null,
                null,
                null
        );
    }
}
