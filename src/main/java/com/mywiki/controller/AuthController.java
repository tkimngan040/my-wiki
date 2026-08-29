package com.mywiki.controller;

import com.mywiki.model.dto.LoginRequest;
import com.mywiki.model.dto.RegisterRequest;
import com.mywiki.service.interfaces.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    ) {
        Authentication authentication = authService.login(request, httpRequest, httpResponse);

        Map<String, String> body = new LinkedHashMap<>();
        body.put("email", authentication.getName());
        body.put("message", "Login successful");

        return ResponseEntity.ok(body);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of("message", "Registration successful")
        );
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrfToken(CsrfToken csrfToken) {
        return ResponseEntity.ok(
                Map.of("token", csrfToken.getToken())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> currentUser(Authentication authentication) {
        return ResponseEntity.ok(Map.of("email", authentication.getName()));
    }
}