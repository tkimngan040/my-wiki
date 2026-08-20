package com.mywiki.service.impl;

import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mywiki.model.dto.UpdateAccountRequest;
import com.mywiki.model.dto.ChangePasswordRequest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void updateAccount_shouldUpdateUserInformation() {

        // Tạo User test
        User user = new User(
                "OldUsername",
                "test_update@mywiki.com",
                passwordEncoder.encode("123456")
        );

        userRepository.save(user);

        // Lưu lại thời gian UpdatedAt trước khi cập nhật
        LocalDateTime oldUpdatedAt = user.getUpdatedAt();

        // Dữ liệu mới muốn cập nhật
        UpdateAccountRequest request = new UpdateAccountRequest(
                "NewUsername",
                "https://example.com/avatar.jpg",
                LocalDate.of(2005, 5, 20),
                "Hello, this is my bio."
        );

        // Gọi updateAccount()
        userService.updateAccount(
                user.getUserId(),
                request
        );

        // Lấy lại User từ database
        User updatedUser = userRepository
                .findById(user.getUserId())
                .orElse(null);

        // Kiểm tra User tồn tại
        assertNotNull(updatedUser);

        // Kiểm tra các thông tin đã được cập nhật
        assertEquals(
                "NewUsername",
                updatedUser.getUsername()
        );

        assertEquals(
                "https://example.com/avatar.jpg",
                updatedUser.getAvatarUrl()
        );

        assertEquals(
                LocalDate.of(2005, 5, 20),
                updatedUser.getDateOfBirth()
        );

        assertEquals(
                "Hello, this is my bio.",
                updatedUser.getBio()
        );

        // Kiểm tra UpdatedAt được cập nhật
        assertTrue(
                updatedUser.getUpdatedAt().isAfter(oldUpdatedAt)
        );

        // Xóa dữ liệu test
        userRepository.delete(updatedUser);
    }

    @Test
    void changePassword_shouldWorkCorrectly() {

        // ===== CASE 1: Đổi password thành công =====
        User user = new User(
                "ChangePasswordUser",
                "test_change_password@mywiki.com",
                passwordEncoder.encode("123456")
        );

        userRepository.save(user);

        ChangePasswordRequest request = new ChangePasswordRequest(
                "123456",
                "654321",
                "654321"
        );

        userService.changePassword(user.getUserId(), request);

        User updatedUser = userRepository
                .findById(user.getUserId())
                .orElse(null);

        assertNotNull(updatedUser);
        assertTrue(
                passwordEncoder.matches(
                        "654321",
                        updatedUser.getPasswordHash()
                )
        );

        // ===== CASE 2: Password mới không khớp =====
        ChangePasswordRequest mismatchRequest = new ChangePasswordRequest(
                "654321",
                "111111",
                "222222"
        );

        assertThrows(
                RuntimeException.class,
                () -> userService.changePassword(
                        user.getUserId(),
                        mismatchRequest
                )
        );

        // ===== CASE 3: Password hiện tại sai =====
        ChangePasswordRequest wrongCurrentRequest = new ChangePasswordRequest(
                "wrongpassword",
                "111111",
                "111111"
        );

        assertThrows(
                RuntimeException.class,
                () -> userService.changePassword(
                        user.getUserId(),
                        wrongCurrentRequest
                )
        );

        userRepository.delete(user);
    }
}