package com.mywiki.service.impl;

import com.mywiki.model.dto.ChangePasswordRequest;
import com.mywiki.model.dto.CurrentUserProfileResponse;
import com.mywiki.model.dto.UpdateAccountRequest;
import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import com.mywiki.service.interfaces.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public CurrentUserProfileResponse getCurrentUserProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return CurrentUserProfileResponse.from(user);
    }

    @Override
    public void updateAccount(Integer userId, UpdateAccountRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getUsername() != null && request.getUsername().length() > 50) {
            throw new IllegalArgumentException("Username must not exceed 50 characters");
        }

        if (request.getAvatarUrl() != null && request.getAvatarUrl().length() > 500) {
            throw new IllegalArgumentException("Avatar URL must not exceed 500 characters");
        }

        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            try {
                URI avatarUri = new URI(request.getAvatarUrl());
                if (!avatarUri.isAbsolute()
                        || avatarUri.getHost() == null
                        || (!"http".equalsIgnoreCase(avatarUri.getScheme())
                        && !"https".equalsIgnoreCase(avatarUri.getScheme()))) {
                    throw new IllegalArgumentException("Avatar URL must be a valid URL");
                }
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Avatar URL must be a valid URL");
            }
        }

        if (request.getBio() != null && request.getBio().length() > 500) {
            throw new IllegalArgumentException("Bio must not exceed 500 characters");
        }

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        userRepository.save(user);
    }

    @Override
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        if (request == null
                || request.getCurrentPassword() == null
                || request.getNewPassword() == null
                || request.getConfirmNewPassword() == null
                || request.getCurrentPassword().isBlank()
                || request.getNewPassword().isBlank()
                || request.getConfirmNewPassword().isBlank()) {
            throw new IllegalArgumentException("All password fields are required");
        }

        if (request.getCurrentPassword().length() < 6
                || request.getNewPassword().length() < 6
                || request.getConfirmNewPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New password and confirmation password do not match");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        userRepository.save(user);
    }
}