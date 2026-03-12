package com.example.internhub_be.service;

import com.example.internhub_be.payload.UserCreationRequest;
import com.example.internhub_be.payload.UserStatusUpdateRequest;
import com.example.internhub_be.payload.UserResponse;

public interface AdminUserService {
    UserResponse createUser(UserCreationRequest request);
    UserResponse updateUserStatus(Long userId, UserStatusUpdateRequest request);
    UserResponse activateUser(String activationToken);
}
