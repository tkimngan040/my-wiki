package com.mywiki.controller;

import com.mywiki.model.dto.LinkResponse;
import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import com.mywiki.service.interfaces.LinkService;
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

class LinkControllerTest {
    private LinkService linkService;
    private UserRepository userRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        linkService = mock(LinkService.class);
        userRepository = mock(UserRepository.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new LinkController(linkService, userRepository)).build();
    }

    @Test
    void getLinks_shouldAllowAnonymousRequest() throws Exception {
        when(linkService.getLinks(10, 20, null)).thenReturn(java.util.List.of(response()));
        mockMvc.perform(get("/api/workspaces/10/pages/20/links"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"linkId\":30,\"sourcePageId\":20,\"targetPageId\":21}]"));
        verify(linkService).getLinks(10, 20, null);
    }

    @Test
    void createLink_shouldReturnCreated() throws Exception {
        when(userRepository.findByEmail("owner@mywiki.com")).thenReturn(Optional.of(user()));
        when(linkService.createLink(eq(1), eq(10), eq(20), any())).thenReturn(response());
        mockMvc.perform(post("/api/workspaces/10/pages/20/links")
                        .principal(authentication()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetPageId\":21,\"anchorText\":\"See details\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().json("{\"linkId\":30,\"targetPageId\":21}"));
        verify(linkService).createLink(eq(1), eq(10), eq(20), any());
    }

    @Test
    void deleteLink_shouldReturnNoContent() throws Exception {
        when(userRepository.findByEmail("owner@mywiki.com")).thenReturn(Optional.of(user()));
        mockMvc.perform(delete("/api/workspaces/10/pages/20/links/30").principal(authentication()))
                .andExpect(status().isNoContent());
        verify(linkService).deleteLink(1, 10, 20, 30);
    }

    private UsernamePasswordAuthenticationToken authentication() {
        return new UsernamePasswordAuthenticationToken("owner@mywiki.com", null);
    }
    private User user() {
        User user = new User("Owner", "owner@mywiki.com", "hash");
        user.setUserId(1);
        return user;
    }
    private LinkResponse response() {
        return new LinkResponse(30, 10, 20, "Source", 21, "Target", "See details", null);
    }
}
