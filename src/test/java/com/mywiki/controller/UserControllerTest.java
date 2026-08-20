package com.mywiki.controller;

import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import com.mywiki.service.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private UserService userService;
    private UserRepository userRepository;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        userService = org.mockito.Mockito.mock(UserService.class);
        userRepository = org.mockito.Mockito.mock(UserRepository.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new UserController(userService, userRepository))
                .build();
    }

    @Test
    void updateAccount_shouldReturnOkAndDelegateToService() throws Exception {
        User user = new User("Old username", "user@mywiki.com", "password");
        user.setUserId(1);
        when(userRepository.findByEmail("user@mywiki.com"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(put("/api/users/me")
                        .principal(authentication("user@mywiki.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "New username",
                                  "avatarUrl": "https://example.com/avatar.jpg",
                                  "dateOfBirth": "2000-01-01",
                                  "bio": "Writer"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"message\":\"Account updated successfully\"}"
                ));

        verify(userService).updateAccount(eq(1), any());
    }

    @Test
    void changePassword_shouldReturnOkAndDelegateToService() throws Exception {
        User user = new User("Username", "user@mywiki.com", "password");
        user.setUserId(1);
        when(userRepository.findByEmail("user@mywiki.com"))
                .thenReturn(Optional.of(user));

        mockMvc.perform(put("/api/users/me/password")
                        .principal(authentication("user@mywiki.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "old-password",
                                  "newPassword": "new-password",
                                  "confirmNewPassword": "new-password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"message\":\"Password changed successfully\"}"
                ));

        verify(userService).changePassword(eq(1), any());
    }

    private UsernamePasswordAuthenticationToken authentication(String email) {
        return new UsernamePasswordAuthenticationToken(email, null);
    }
}
