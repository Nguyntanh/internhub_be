package com.example.internhub_be.service;

import com.example.internhub_be.domain.Department;
import com.example.internhub_be.domain.Role;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.exception.EmailAlreadyExistsException;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.UserCreationRequest;
import com.example.internhub_be.payload.UserStatusUpdateRequest;
import com.example.internhub_be.payload.UserResponse; // Import UserResponse
import com.example.internhub_be.repository.DepartmentRepository;
import com.example.internhub_be.repository.RoleRepository;
import com.example.internhub_be.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditLogService auditLogService;

    public AdminUserServiceImpl(UserRepository userRepository,
                                RoleRepository roleRepository,
                                DepartmentRepository departmentRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService,
                                AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreationRequest request) { // Changed return type to UserResponse
        // Kiểm tra email trùng lặp
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email đã tồn tại: " + request.getEmail());
        }

        // Tìm Role và Department
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));

        // Tạo người dùng mới
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setRole(role);
        user.setDepartment(department);
        user.setPhone(request.getPhone());
        user.setIsActive(false); // Mặc định là không hoạt động
        user.setActivationToken(UUID.randomUUID().toString()); // Tự động sinh activation_token

        // Tạo mật khẩu tạm thời và mã hóa
        String tempPassword = UUID.randomUUID().toString().substring(0, 8); // Mật khẩu tạm thời 8 ký tự
        user.setPassword(passwordEncoder.encode(tempPassword));

        User savedUser = userRepository.save(user);

        // Gửi email kích hoạt
        String activationLink = "http://localhost:4200/activate?token=" + savedUser.getActivationToken();
        emailService.sendActivationEmail(savedUser.getEmail(), activationLink, tempPassword);

        // Ghi nhật ký audit
        Map<String, Object> details = new HashMap<>();
        details.put("action", "CREATE_USER");
        details.put("target_user_id", savedUser.getId());
        details.put("target_user_email", savedUser.getEmail());
        // In a real application, 'performedBy' would be the authenticated admin user
        // For now, we assume a placeholder or get it from security context
        auditLogService.logAction("USER_CREATED", null, details); // Pass null for performedBy for now, will fix later

        return mapUserToUserResponse(savedUser); // Map User to UserResponse
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, UserStatusUpdateRequest request) { // Changed return type to UserResponse
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Boolean oldStatus = user.getIsActive();
        user.setIsActive(request.getIsActive());
        User updatedUser = userRepository.save(user);

        // Ghi nhật ký audit
        Map<String, Object> details = new HashMap<>();
        details.put("action", "UPDATE_USER_STATUS");
        details.put("target_user_id", updatedUser.getId());
        details.put("target_user_email", updatedUser.getEmail());
        details.put("old_status", oldStatus);
        details.put("new_status", updatedUser.getIsActive());
        // For now, we assume a placeholder or get it from security context
        auditLogService.logAction("USER_STATUS_UPDATED", null, details); // Pass null for performedBy for now, will fix later

        return mapUserToUserResponse(updatedUser); // Map User to UserResponse
    }

    @Override
    @Transactional
    public UserResponse activateUser(String activationToken) {
        User user = userRepository.findByActivationToken(activationToken)
                .orElseThrow(() -> new ResourceNotFoundException("User", "activation token", activationToken));

        user.setIsActive(true);
        user.setActivationToken(null); // Clear the activation token after activation
        User activatedUser = userRepository.save(user);

        // Ghi nhật ký audit
        Map<String, Object> details = new HashMap<>();
        details.put("action", "ACTIVATE_USER");
        details.put("target_user_id", activatedUser.getId());
        details.put("target_user_email", activatedUser.getEmail());
        auditLogService.logAction("USER_ACTIVATED", null, details);

        return mapUserToUserResponse(activatedUser);
    }

    private UserResponse mapUserToUserResponse(User user) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setName(user.getName());
        userResponse.setEmail(user.getEmail());
        userResponse.setIsActive(user.getIsActive());
        userResponse.setPhone(user.getPhone());
        userResponse.setAvatar(user.getAvatar());
        userResponse.setCreatedAt(user.getCreatedAt());

        if (user.getRole() != null) {
            userResponse.setRoleId(user.getRole().getId());
            userResponse.setRoleName(user.getRole().getName());
        }
        if (user.getDepartment() != null) {
            userResponse.setDepartmentId(user.getDepartment().getId());
            userResponse.setDepartmentName(user.getDepartment().getName());
        }
        return userResponse;
    }
}
