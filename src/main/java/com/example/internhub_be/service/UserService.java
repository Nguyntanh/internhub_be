package com.example.internhub_be.service;

import com.example.internhub_be.payload.ChangePasswordRequest;
import com.example.internhub_be.payload.NewAvatarUrlResponse;
import com.example.internhub_be.payload.PagedResponse;
import com.example.internhub_be.payload.UserProfileResponse;
import com.example.internhub_be.payload.UserResponse;

import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserProfileResponse getUserProfile(String email);
    void changePassword(String email, ChangePasswordRequest changePasswordRequest);
    NewAvatarUrlResponse updateAvatar(String email, MultipartFile file);
    PagedResponse<UserResponse> getAllUsers(Long roleId, int page, int size);
}
