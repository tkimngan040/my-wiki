package com.mywiki.service.interfaces;

import com.mywiki.model.dto.RegisterRequest;
import com.mywiki.model.dto.LoginRequest;
import com.mywiki.model.dto.UpdateAccountRequest;


public interface UserService {

    void register(RegisterRequest request);
    boolean login(LoginRequest request);
    void updateAccount(Integer userId, UpdateAccountRequest request);

}