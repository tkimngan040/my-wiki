package com.mywiki.service.impl;

import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mywiki.model.dto.RegisterRequest;
import com.mywiki.model.dto.LoginRequest;
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
    void register_shouldCreateUserWithHashedPassword() {

        // Dữ liệu test
        String username = "TestUser";
        String email = "test_register@mywiki.com";
        String password = "123456";

        RegisterRequest request =
                new RegisterRequest(username, email, password);

        // Gọi register()
        userService.register(request);

        // Lấy User vừa lưu từ database
        User savedUser =
                userRepository.findByEmail(email).orElse(null);

        // Kiểm tra User đã được lưu
        assertNotNull(savedUser);

        // Kiểm tra thông tin cơ bản
        assertEquals(username, savedUser.getUsername());
        assertEquals(email, savedUser.getEmail());

        // Password trong DB không được là password gốc
        assertNotEquals(password, savedUser.getPasswordHash());

        // Kiểm tra password gốc khớp với hash
        assertTrue(
                passwordEncoder.matches(
                        password,
                        savedUser.getPasswordHash()
                )
        );

        // Xóa dữ liệu test sau khi kiểm tra
        userRepository.delete(savedUser);
    }

    @Test
    void login_shouldAuthenticateUserCorrectly() {

        // Tạo User test
        String email = "test_login@mywiki.com";
        String password = "123456";
        String passwordHash = passwordEncoder.encode(password);

        User user = new User(
                "LoginTestUser",
                email,
                passwordHash
        );

        userRepository.save(user);

        // 1. Email đúng + password đúng
        LoginRequest correctRequest =
                new LoginRequest(email, password);

        assertTrue(userService.login(correctRequest));


        // 2. Email đúng + password sai
        LoginRequest wrongPasswordRequest =
                new LoginRequest(email, "wrongpassword");

        assertFalse(userService.login(wrongPasswordRequest));


        // 3. Email không tồn tại
        LoginRequest wrongEmailRequest =
                new LoginRequest(
                        "not_exist@mywiki.com",
                        password
                );

        assertFalse(userService.login(wrongEmailRequest));


        // Xóa User test
        userRepository.delete(user);
    }

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