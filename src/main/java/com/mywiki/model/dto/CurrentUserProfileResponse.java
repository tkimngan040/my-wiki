package com.mywiki.model.dto;

import com.mywiki.model.entity.User;

import java.time.LocalDate;

public record CurrentUserProfileResponse(
        Integer userId,
        String username,
        String email,
        String avatarUrl,
        LocalDate dateOfBirth,
        String bio
) {
    public static CurrentUserProfileResponse from(User user) {
        return new CurrentUserProfileResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getDateOfBirth(),
                user.getBio()
        );
    }
}
