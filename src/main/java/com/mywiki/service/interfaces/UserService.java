package com.mywiki.service.interfaces;

import com.mywiki.model.dto.ChangePasswordRequest;
import com.mywiki.model.dto.CurrentUserProfileResponse;
import com.mywiki.model.dto.UpdateAccountRequest;

public interface UserService {

    CurrentUserProfileResponse getCurrentUserProfile(Integer userId);

    void updateAccount(Integer userId, UpdateAccountRequest request);

    void changePassword(Integer userId, ChangePasswordRequest request);

}