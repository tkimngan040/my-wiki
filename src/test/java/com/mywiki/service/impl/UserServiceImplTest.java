package com.mywiki.service.impl;

import com.mywiki.model.dto.RegisterRequest;
import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

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
}