package com.example.internhub_be.controller;

import com.example.internhub_be.domain.User;
import com.example.internhub_be.payload.ChangePasswordRequest;
import com.example.internhub_be.payload.NewAvatarUrlResponse;
import com.example.internhub_be.payload.UserProfileResponse;
import com.example.internhub_be.payload.UserResponse;
import com.example.internhub_be.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        UserProfileResponse userProfile = userService.getUserProfile(currentPrincipalName);
        return ResponseEntity.ok(userProfile);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        userService.changePassword(currentPrincipalName, changePasswordRequest);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully!"));
    }

    @PatchMapping(value = "/profile/avatar", consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NewAvatarUrlResponse> updateAvatar(@RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        NewAvatarUrlResponse newAvatarUrlResponse = userService.updateAvatar(currentPrincipalName, file);
        return ResponseEntity.ok(newAvatarUrlResponse);
    }

    @GetMapping("/mentors")
    public ResponseEntity<List<User>> getMentors() {
        return ResponseEntity.ok(userService.getUsersByRole("MENTOR"));
    }

    @GetMapping("/managers")
    public ResponseEntity<List<User>> getManagers() {
        return ResponseEntity.ok(userService.getUsersByRole("MANAGER"));
    }

    // THÊM MỚI: lấy danh sách user role INTERN chưa có hồ sơ thực tập
    @GetMapping("/interns/available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<UserResponse>> getAvailableInterns() {
        return ResponseEntity.ok(userService.getAvailableInterns());
    }
}