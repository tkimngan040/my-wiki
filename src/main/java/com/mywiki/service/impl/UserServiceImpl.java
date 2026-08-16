package com.mywiki.service.impl;

import com.mywiki.model.dto.RegisterRequest;
import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import com.mywiki.service.interfaces.UserService;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(RegisterRequest request) {

        // Kiểm tra email đã tồn tại chưa
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists!");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());

       // Tạo User mới
        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordHash
        );

        // Lưu User vào database
        userRepository.save(user);
    }
}