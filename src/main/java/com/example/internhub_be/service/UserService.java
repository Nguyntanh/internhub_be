package com.example.internhub_be.service;

import com.example.internhub_be.payload.ChangePasswordRequest;
import com.example.internhub_be.payload.UserProfileResponse;

public interface UserService {
    UserProfileResponse getUserProfile(String email);
    void changePassword(String email, ChangePasswordRequest changePasswordRequest);
}
