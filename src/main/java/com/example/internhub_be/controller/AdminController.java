package com.example.internhub_be.controller;

import com.example.internhub_be.payload.UserCreationRequest;
import com.example.internhub_be.payload.UserStatusUpdateRequest;
import com.example.internhub_be.payload.UserResponse;
import com.example.internhub_be.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminController {

    private final AdminUserService adminUserService;

    public AdminController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    /**
     * POST /api/admin/users
     * Tạo tài khoản người dùng mới (chỉ ADMIN).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreationRequest request) {
        UserResponse newUser = adminUserService.createUser(request);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    /**
     * PATCH /api/admin/users/{id}/status
     * Cập nhật trạng thái hoạt động của người dùng (chỉ ADMIN).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateUserStatus(
            @PathVariable("id") Long userId,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        UserResponse updatedUser = adminUserService.updateUserStatus(userId, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * GET /api/admin/users/all
     * Lấy toàn bộ danh sách người dùng (ADMIN và HR dùng cho picker thêm nhân sự).
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'MANAGER', 'MENTOR')")
    @GetMapping("/all")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = adminUserService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}