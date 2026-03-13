package com.example.internhub_be.service;

import com.example.internhub_be.payload.UserProfileResponse;

public interface UserService {
    UserProfileResponse getUserProfile(String email);
}
