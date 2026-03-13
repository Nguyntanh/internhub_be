package com.example.internhub_be.service;

import com.example.internhub_be.payload.RolePermissionRequest;
import com.example.internhub_be.payload.RolePermissionResponse;

import java.util.List;

public interface RolePermissionService {
    RolePermissionResponse createOrUpdateRolePermission(RolePermissionRequest request);
    List<RolePermissionResponse> getRolePermissionsByRoleId(Long roleId);
    void deleteRolePermission(Long roleId, Long functionId);
    RolePermissionResponse getRolePermissionById(Long roleId, Long functionId);
    List<RolePermissionResponse> getAllRolePermissions(); // New method
}
