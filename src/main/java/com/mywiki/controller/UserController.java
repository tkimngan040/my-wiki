package com.mywiki.controller;

import com.mywiki.model.dto.ChangePasswordRequest;
import com.mywiki.model.dto.UpdateAccountRequest;
import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import com.mywiki.service.interfaces.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(
            UserService userService,
            UserRepository userRepository
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PutMapping("/me")
    public ResponseEntity<Map<String, String>> updateAccount(
            @Valid @RequestBody UpdateAccountRequest request,
            Authentication authentication
    ) {
        userService.updateAccount(getCurrentUserId(authentication), request);

        return ResponseEntity.ok(
                Map.of("message", "Account updated successfully")
        );
    }

    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        userService.changePassword(getCurrentUserId(authentication), request);

        return ResponseEntity.ok(
                Map.of("message", "Password changed successfully")
        );
    }

    private Integer getCurrentUserId(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return user.getUserId();
    }
}
