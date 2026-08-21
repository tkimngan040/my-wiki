package com.mywiki.controller;

import com.mywiki.model.dto.WorkspaceResponse;
import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import com.mywiki.service.interfaces.WorkspaceService;
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

class WorkspaceControllerTest {

    private WorkspaceService workspaceService;
    private UserRepository userRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        workspaceService = mock(WorkspaceService.class);
        userRepository = mock(UserRepository.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new WorkspaceController(workspaceService, userRepository))
                .build();
    }

    @Test
    void getWorkspaces_shouldReturnVisibleWorkspacesForAuthenticatedUser() throws Exception {
        User user = user("user@mywiki.com", 1);
        when(userRepository.findByEmail("user@mywiki.com"))
                .thenReturn(Optional.of(user));
        when(workspaceService.getVisibleWorkspaces(1))
                .thenReturn(List.of(response(10, "World", "Private")));

        mockMvc.perform(get("/api/workspaces")
                        .principal(authentication("user@mywiki.com")))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [
                          {
                            "workspaceId": 10,
                            "name": "World",
                            "visibility": "Private"
                          }
                        ]
                        """));

        verify(workspaceService).getVisibleWorkspaces(1);
    }

    @Test
    void getWorkspace_shouldAllowAnonymousRequest() throws Exception {
        when(workspaceService.getWorkspace(10, null))
                .thenReturn(response(10, "Public World", "Public"));

        mockMvc.perform(get("/api/workspaces/10"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "workspaceId": 10,
                          "name": "Public World",
                          "visibility": "Public"
                        }
                        """));

        verify(workspaceService).getWorkspace(10, null);
    }

    @Test
    void createWorkspace_shouldReturnCreatedAndDelegateToService() throws Exception {
        User user = user("user@mywiki.com", 1);
        when(userRepository.findByEmail("user@mywiki.com"))
                .thenReturn(Optional.of(user));
        when(workspaceService.createWorkspace(eq(1), any()))
                .thenReturn(response(10, "World", "Private"));

        mockMvc.perform(post("/api/workspaces")
                        .principal(authentication("user@mywiki.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "World",
                                  "description": "A novel world",
                                  "visibility": "Private"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().json("""
                        {
                          "workspaceId": 10,
                          "name": "World",
                          "visibility": "Private"
                        }
                        """));

        verify(workspaceService).createWorkspace(eq(1), any());
    }

    @Test
    void updateWorkspace_shouldDelegateToService() throws Exception {
        User user = user("user@mywiki.com", 1);
        when(userRepository.findByEmail("user@mywiki.com"))
                .thenReturn(Optional.of(user));
        when(workspaceService.updateWorkspace(eq(1), eq(10), any()))
                .thenReturn(response(10, "Updated World", "Public"));

        mockMvc.perform(put("/api/workspaces/10")
                        .principal(authentication("user@mywiki.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated World",
                                  "description": "Updated",
                                  "visibility": "Public"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "workspaceId": 10,
                          "name": "Updated World",
                          "visibility": "Public"
                        }
                        """));

        verify(workspaceService).updateWorkspace(eq(1), eq(10), any());
    }

    @Test
    void deleteWorkspace_shouldReturnNoContent() throws Exception {
        User user = user("user@mywiki.com", 1);
        when(userRepository.findByEmail("user@mywiki.com"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/workspaces/10")
                        .principal(authentication("user@mywiki.com")))
                .andExpect(status().isNoContent());

        verify(workspaceService).deleteWorkspace(1, 10);
    }

    private UsernamePasswordAuthenticationToken authentication(String email) {
        return new UsernamePasswordAuthenticationToken(email, null);
    }

    private User user(String email, int id) {
        User user = new User("Test User", email, "hash");
        user.setUserId(id);
        return user;
    }

    private WorkspaceResponse response(int id, String name, String visibility) {
        return new WorkspaceResponse(
                id,
                1,
                "Test User",
                name,
                null,
                visibility,
                null,
                null
        );
    }
}
