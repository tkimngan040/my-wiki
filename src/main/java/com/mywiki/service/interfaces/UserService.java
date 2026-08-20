package com.mywiki.service.interfaces;

import com.mywiki.model.dto.UpdateAccountRequest;
import com.mywiki.model.dto.ChangePasswordRequest;


public interface UserService {

    void updateAccount(Integer userId, UpdateAccountRequest request);
    void changePassword(Integer userId, ChangePasswordRequest request);

}