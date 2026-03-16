package com.example.internhub_be.controller;

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

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return ResponseEntity.ok(userService.getUserProfile(email));
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        userService.changePassword(email, request);

        return ResponseEntity.ok(
                Map.of("message", "Password changed successfully!")
        );
    }

    @PatchMapping(value = "/profile/avatar", consumes = "multipart/form-data")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NewAvatarUrlResponse> updateAvatar(
            @RequestParam("file") MultipartFile file) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return ResponseEntity.ok(userService.updateAvatar(email, file));
    }

    // ⭐ Mentors
    @GetMapping("/mentors")
    public ResponseEntity<List<UserResponse>> getMentors() {
        return ResponseEntity.ok(userService.getUsersByRoleResponse("MENTOR"));
    }

    // ⭐ Managers
    @GetMapping("/managers")
    public ResponseEntity<List<UserResponse>> getManagers() {
        return ResponseEntity.ok(userService.getUsersByRoleResponse("MANAGER"));
    }

    // ⭐ Interns
    @GetMapping("/interns")
    public ResponseEntity<List<UserResponse>> getInterns() {
        return ResponseEntity.ok(userService.getUsersByRoleResponse("INTERN"));
    }
}