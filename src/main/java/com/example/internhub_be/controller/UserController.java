package com.example.internhub_be.controller;

import com.example.internhub_be.payload.ChangePasswordRequest;
import com.example.internhub_be.payload.UserProfileResponse;
import com.example.internhub_be.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()") // Ensure user is authenticated to access their profile
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName(); // This will be the email

        UserProfileResponse userProfile = userService.getUserProfile(currentPrincipalName);
        return ResponseEntity.ok(userProfile);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()") // Only authenticated users can change their password
    public ResponseEntity<String> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName(); // This will be the email

        userService.changePassword(currentPrincipalName, changePasswordRequest);
        return ResponseEntity.ok("Password changed successfully!");
    }
}
