package com.example.internhub_be.service;

import com.example.internhub_be.domain.Function;
import com.example.internhub_be.domain.Role;
import com.example.internhub_be.domain.RolePermission;
import com.example.internhub_be.domain.RolePermissionId;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.RolePermissionRequest;
import com.example.internhub_be.payload.RolePermissionResponse;
import com.example.internhub_be.repository.FunctionRepository;
import com.example.internhub_be.repository.RolePermissionRepository;
import com.example.internhub_be.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RoleRepository roleRepository;
    private final FunctionRepository functionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    public RolePermissionServiceImpl(RoleRepository roleRepository,
                                     FunctionRepository functionRepository,
                                     RolePermissionRepository rolePermissionRepository) {
        this.roleRepository = roleRepository;
        this.functionRepository = functionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    @Transactional
    public RolePermissionResponse createOrUpdateRolePermission(RolePermissionRequest request) {
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
        Function function = functionRepository.findById(request.getFunctionId())
                .orElseThrow(() -> new ResourceNotFoundException("Function", "id", request.getFunctionId()));

        RolePermissionId id = new RolePermissionId(request.getRoleId(), request.getFunctionId());
        RolePermission rolePermission = rolePermissionRepository.findById(id)
                .orElse(new RolePermission(id, role, function, false, false, false, false));

        rolePermission.setCanAccess(request.getCanAccess());
        rolePermission.setCanCreate(request.getCanCreate());
        rolePermission.setCanEdit(request.getCanEdit());
        rolePermission.setCanDelete(request.getCanDelete());

        RolePermission savedRolePermission = rolePermissionRepository.save(rolePermission);
        return mapRolePermissionToResponse(savedRolePermission);
    }

    @Override
    public List<RolePermissionResponse> getRolePermissionsByRoleId(Long roleId) {
        roleRepository.findById(roleId) // Check if role exists
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId));

        List<RolePermission> rolePermissions = rolePermissionRepository.findById_RoleId(roleId);
        return rolePermissions.stream()
                .map(this::mapRolePermissionToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRolePermission(Long roleId, Long functionId) {
        RolePermissionId id = new RolePermissionId(roleId, functionId);
        RolePermission rolePermission = rolePermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RolePermission", "roleId and functionId", id.toString()));
        rolePermissionRepository.delete(rolePermission);
    }

    @Override
    public RolePermissionResponse getRolePermissionById(Long roleId, Long functionId) {
        RolePermissionId id = new RolePermissionId(roleId, functionId);
        RolePermission rolePermission = rolePermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RolePermission", "roleId and functionId", id.toString()));
        return mapRolePermissionToResponse(rolePermission);
    }

    @Override
    public List<RolePermissionResponse> getAllRolePermissions() {
        List<RolePermission> allRolePermissions = rolePermissionRepository.findAll();
        return allRolePermissions.stream()
                .map(this::mapRolePermissionToResponse)
                .collect(Collectors.toList());
    }

    private RolePermissionResponse mapRolePermissionToResponse(RolePermission rolePermission) {
        RolePermissionResponse response = new RolePermissionResponse();
        response.setRoleId(rolePermission.getRole().getId());
        response.setRoleName(rolePermission.getRole().getName());
        response.setFunctionId(rolePermission.getFunction().getId());
        response.setFunctionCode(rolePermission.getFunction().getCode());
        response.setFunctionName(rolePermission.getFunction().getName());
        response.setCanAccess(rolePermission.getCanAccess());
        response.setCanCreate(rolePermission.getCanCreate());
        response.setCanEdit(rolePermission.getCanEdit());
        response.setCanDelete(rolePermission.getCanDelete());
        return response;
    }
}
