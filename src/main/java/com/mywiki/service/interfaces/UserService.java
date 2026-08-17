package com.mywiki.service.interfaces;

import com.mywiki.model.dto.RegisterRequest;
import com.mywiki.model.dto.LoginRequest;

public interface UserService {

    void register(RegisterRequest request);
    boolean login(LoginRequest request);
}