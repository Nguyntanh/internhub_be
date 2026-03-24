package com.example.internhub_be.controller;

import com.example.internhub_be.payload.RolePermissionRequest;
import com.example.internhub_be.payload.RolePermissionResponse;
import com.example.internhub_be.payload.response.RoleResponse;
import com.example.internhub_be.service.RolePermissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    public RolePermissionController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = rolePermissionService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/role-permissions")
    public ResponseEntity<RolePermissionResponse> createOrUpdateRolePermission(@Valid @RequestBody RolePermissionRequest request) {
        RolePermissionResponse response = rolePermissionService.createOrUpdateRolePermission(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/role-permissions") // Map this to GET /api/admin/role-permissions
    public ResponseEntity<List<RolePermissionResponse>> getAllRolePermissions() {
        List<RolePermissionResponse> allPermissions = rolePermissionService.getAllRolePermissions();
        return ResponseEntity.ok(allPermissions);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/role-permissions/{roleId}")
    public ResponseEntity<List<RolePermissionResponse>> getRolePermissionsByRoleId(@PathVariable Long roleId) {
        List<RolePermissionResponse> responses = rolePermissionService.getRolePermissionsByRoleId(roleId);
        return ResponseEntity.ok(responses);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/role-permissions/{roleId}/{functionId}")
    public ResponseEntity<RolePermissionResponse> getRolePermissionById(@PathVariable Long roleId, @PathVariable Long functionId) {
        RolePermissionResponse response = rolePermissionService.getRolePermissionById(roleId, functionId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/role-permissions/{roleId}/{functionId}")
    public ResponseEntity<String> deleteRolePermission(@PathVariable Long roleId, @PathVariable Long functionId) {
        rolePermissionService.deleteRolePermission(roleId, functionId);
        return ResponseEntity.ok("Xóa quyền hạn vai trò thành công.");
    }
}
