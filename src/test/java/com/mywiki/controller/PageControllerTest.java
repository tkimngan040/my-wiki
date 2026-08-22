package com.mywiki.controller;

import com.mywiki.model.dto.PageResponse;
import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import com.mywiki.service.interfaces.PageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PageControllerTest {
    private PageService pageService;
    private UserRepository userRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        pageService = mock(PageService.class);
        userRepository = mock(UserRepository.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new PageController(pageService, userRepository)).build();
    }

    @Test
    void getPage_shouldAllowAnonymousRequest() throws Exception {
        when(pageService.getPage(10, 20, null)).thenReturn(response(20, "Overview"));
        mockMvc.perform(get("/api/workspaces/10/pages/20"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"pageId\":20,\"workspaceId\":10,\"title\":\"Overview\"}"));
        verify(pageService).getPage(10, 20, null);
    }

    @Test
    void createPage_shouldReturnCreated() throws Exception {
        when(userRepository.findByEmail("owner@mywiki.com"))
                .thenReturn(Optional.of(user(1)));
        when(pageService.createPage(eq(1), eq(10), any()))
                .thenReturn(response(20, "Overview"));
        mockMvc.perform(post("/api/workspaces/10/pages")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Overview\",\"content\":\"Text\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"pageId\":20,\"title\":\"Overview\"}"));
        verify(pageService).createPage(eq(1), eq(10), any());
    }

    @Test
    void updatePage_shouldDelegateToService() throws Exception {
        when(userRepository.findByEmail("owner@mywiki.com"))
                .thenReturn(Optional.of(user(1)));
        when(pageService.updatePage(eq(1), eq(10), eq(20), any()))
                .thenReturn(response(20, "Updated"));
        mockMvc.perform(put("/api/workspaces/10/pages/20")
                        .principal(authentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\",\"content\":\"New text\"}"))
                .andExpect(status().isOk());
        verify(pageService).updatePage(eq(1), eq(10), eq(20), any());
    }

    @Test
    void deletePage_shouldReturnNoContent() throws Exception {
        when(userRepository.findByEmail("owner@mywiki.com"))
                .thenReturn(Optional.of(user(1)));
        mockMvc.perform(delete("/api/workspaces/10/pages/20").principal(authentication()))
                .andExpect(status().isNoContent());
        verify(pageService).deletePage(1, 10, 20);
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken("owner@mywiki.com", null);
    }
    private User user(int id) {
        User user = new User("Owner", "owner@mywiki.com", "hash");
        user.setUserId(id);
        return user;
    }
    private PageResponse response(int id, String title) {
        return new PageResponse(id, 10, null, title, "Text", null, null);
    }
}
