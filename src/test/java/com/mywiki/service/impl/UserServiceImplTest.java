package com.mywiki.service.impl;

import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.mywiki.model.dto.RegisterRequest;
import com.mywiki.model.dto.LoginRequest;

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
}