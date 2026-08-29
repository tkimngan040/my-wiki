package com.mywiki.model.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateAccountRequest {

    @Size(max = 50, message = "Username must not exceed 50 characters")
    private String username;

    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    private String avatarUrl;

    private LocalDate dateOfBirth;

    @Size(max = 500, message = "Bio must not exceed 500 characters")
    private String bio;

    public UpdateAccountRequest() {
    }

    public UpdateAccountRequest(
            String username,
            String avatarUrl,
            LocalDate dateOfBirth,
            String bio
    ) {
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.dateOfBirth = dateOfBirth;
        this.bio = bio;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}