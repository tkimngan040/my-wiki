package com.mywiki.controller;

import com.mywiki.exception.GlobalExceptionHandler;
import com.mywiki.model.dto.LoginRequest;
import com.mywiki.model.dto.RegisterRequest;
import com.mywiki.service.interfaces.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_shouldReturnOk() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@mywiki.com");
        when(authService.login(
                any(LoginRequest.class),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        )).thenReturn(authentication);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@mywiki.com",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"message\":\"Login successful\",\"email\":\"user@mywiki.com\"}"
                ));
    }

    @Test
    void login_withInvalidCredentials_shouldReturnUnauthorized() throws Exception {
        when(authService.login(
                any(LoginRequest.class),
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        )).thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@mywiki.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.path").value("/api/auth/login"));
    }

    @Test
    void register_shouldReturnCreated() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "Username",
                                  "email": "user@mywiki.com",
                                  "password": "password"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().json(
                        "{\"message\":\"Registration successful\"}"
                ));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void logout_shouldReturnNoContent() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(authService).logout(
                any(HttpServletRequest.class),
                any(HttpServletResponse.class)
        );
    }

    @Test
    void currentUser_shouldReturnAuthenticatedEmail() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@mywiki.com");

        mockMvc.perform(get("/api/auth/me")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"email\":\"user@mywiki.com\"}"
                ));
    }

}
