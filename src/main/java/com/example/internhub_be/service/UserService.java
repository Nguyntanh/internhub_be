package com.example.internhub_be.service;

import com.example.internhub_be.domain.User;
import com.example.internhub_be.payload.ChangePasswordRequest;
import com.example.internhub_be.payload.NewAvatarUrlResponse;
import com.example.internhub_be.payload.UserProfileResponse;
import com.example.internhub_be.payload.UserResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    UserProfileResponse getUserProfile(String email);

    void changePassword(String email, ChangePasswordRequest changePasswordRequest);

    NewAvatarUrlResponse updateAvatar(String email, MultipartFile file);

    List<User> getUsersByRole(String roleName);

    // API cho FE lấy danh sách intern
    List<UserResponse> getInterns();

    List<UserResponse> getUsersByRoleResponse(String roleName);
}