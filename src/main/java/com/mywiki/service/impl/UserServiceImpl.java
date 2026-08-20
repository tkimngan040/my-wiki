package com.mywiki.service.impl;

import com.mywiki.model.dto.LoginRequest;
import com.mywiki.model.dto.RegisterRequest;
import com.mywiki.model.dto.UpdateAccountRequest;

import com.mywiki.model.entity.User;
import com.mywiki.repository.UserRepository;
import com.mywiki.service.interfaces.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder)
    {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) // Kiểm tra email đã tồn tại chưa
        {
            throw new RuntimeException("Email already exists");
        }
        String passwordHash = passwordEncoder.encode(request.getPassword()); // Hash password
        User user = new User(request.getUsername(), request.getEmail(), passwordHash);// Tạo User mới
        userRepository.save(user); // Lưu User vào database
    }

    @Override
    public boolean login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null); // Tìm User bằng email
        if (user == null) {return false;}// Không tìm thấy User
        return passwordEncoder.matches(request.getPassword(), user.getPasswordHash()); // Kiểm tra password
    }

    @Override
    public void updateAccount(Integer userId, UpdateAccountRequest request)
    {
        // Tìm User hiện tại
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Cập nhật thông tin
        user.setUsername(request.getUsername());
        user.setAvatarUrl(request.getAvatarUrl());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setBio(request.getBio());

        userRepository.save(user); // Lưu lại User
    }

}