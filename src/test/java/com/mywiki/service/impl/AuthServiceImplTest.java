package com.mywiki.service.impl;

import com.mywiki.model.dto.LoginRequest;
import com.mywiki.model.dto.RegisterRequest;
import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AuthServiceImplTest {

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void register_shouldCreateUserWithHashedPassword() {
        String username = "AuthRegisterTestUser";
        String email = "test_auth_register@mywiki.com";
        String password = "123456";

        authService.register(new RegisterRequest(username, email, password));

        User savedUser = userRepository.findByEmail(email).orElse(null);

        assertNotNull(savedUser);
        assertEquals(username, savedUser.getUsername());
        assertEquals(email, savedUser.getEmail());
        assertTrue(passwordEncoder.matches(password, savedUser.getPasswordHash()));

        userRepository.delete(savedUser);
    }

    @Test
    void login_shouldAuthenticateUserAndCreateSession() {
        String email = "test_auth_login@mywiki.com";
        String password = "123456";
        User user = saveUser("AuthLoginTestUser", email, password);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        Authentication authentication = authService.login(
                new LoginRequest(email, password),
                request,
                response
        );

        assertEquals(email, authentication.getName());
        assertTrue(authentication.isAuthenticated());
        assertNotNull(request.getSession(false));

        userRepository.delete(user);
    }

    @Test
    void login_shouldRejectInvalidCredentials() {
        String email = "test_auth_invalid@mywiki.com";
        String password = "123456";
        User user = saveUser("AuthInvalidTestUser", email, password);

        assertThrows(
                BadCredentialsException.class,
                () -> authService.login(
                        new LoginRequest(email, "wrong-password"),
                        new MockHttpServletRequest(),
                        new MockHttpServletResponse()
                )
        );

        userRepository.delete(user);
    }

    @Test
    void logout_shouldClearContextInvalidateSessionAndDeleteCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = (MockHttpSession) request.getSession(true);

        authService.logout(request, response);

        assertTrue(session.isInvalid());
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        String clearedCookie = response.getHeader("Set-Cookie");
        assertNotNull(clearedCookie);
        assertTrue(clearedCookie.contains("JSESSIONID="));
        assertTrue(clearedCookie.contains("Max-Age=0"));
        assertTrue(clearedCookie.contains("Path=/"));
        assertTrue(clearedCookie.contains("HttpOnly"));
        assertTrue(clearedCookie.contains("SameSite=Lax"));
    }

    private User saveUser(String username, String email, String password) {
        return userRepository.save(
                new User(username, email, passwordEncoder.encode(password))
        );
    }
}
