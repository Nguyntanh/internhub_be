package com.example.internhub_be.controller;

import com.example.internhub_be.payload.UserCreationRequest;
import com.example.internhub_be.payload.UserStatusUpdateRequest;
import com.example.internhub_be.payload.UserResponse; // Import UserResponse
import com.example.internhub_be.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final AdminUserService adminUserService;

    public AdminController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreationRequest request) { // Changed return type to UserResponse
        UserResponse newUser = adminUserService.createUser(request);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable("id") Long userId, // Changed return type to UserResponse
                                                 @Valid @RequestBody UserStatusUpdateRequest request) {
        UserResponse updatedUser = adminUserService.updateUserStatus(userId, request);
        return ResponseEntity.ok(updatedUser);
    }
}
