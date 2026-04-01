package com.example.internhub_be.service;

import com.example.internhub_be.domain.Department;
import com.example.internhub_be.domain.Function;
import com.example.internhub_be.domain.Role;
import com.example.internhub_be.domain.RolePermission;
import com.example.internhub_be.domain.RolePermissionId;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.repository.DepartmentRepository;
import com.example.internhub_be.repository.FunctionRepository;
import com.example.internhub_be.repository.RolePermissionRepository;
import com.example.internhub_be.repository.RoleRepository;
import com.example.internhub_be.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class AdminInitializerService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final RoleRepository roleRepository;
    private final FunctionRepository functionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializerService(UserRepository userRepository, DepartmentRepository departmentRepository,
                                   RoleRepository roleRepository, FunctionRepository functionRepository,
                                   RolePermissionRepository rolePermissionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.roleRepository = roleRepository;
        this.functionRepository = functionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        createDefaultDepartment();
        createDefaultRoles();
        createDefaultFunctions();
        createAdminUser();
    }

    private void createDefaultDepartment() {
        if (departmentRepository.findByName("IT Department").isEmpty()) {
            Department itDepartment = new Department();
            itDepartment.setName("IT Department");
            itDepartment.setDescription("Information Technology Department");
            departmentRepository.save(itDepartment);
        }
    }

    private void createDefaultRoles() {
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            roleRepository.save(new Role(null, "ADMIN"));
        }
        if (roleRepository.findByName("HR").isEmpty()) {
            roleRepository.save(new Role(null, "HR"));
        }
        if (roleRepository.findByName("MANAGER").isEmpty()) {
            roleRepository.save(new Role(null, "MANAGER"));
        }
        if (roleRepository.findByName("MENTOR").isEmpty()) {
            roleRepository.save(new Role(null, "MENTOR"));
        }
        if (roleRepository.findByName("INTERN").isEmpty()) {
            roleRepository.save(new Role(null, "INTERN"));
        }
    }

    private void createDefaultFunctions() {
        if (functionRepository.findByCode("USER_MGMT").isEmpty()) {
            functionRepository.save(new Function(null, "Quản lý người dùng", "USER_MGMT"));
        }
        if (functionRepository.findByCode("SKILL_LIB").isEmpty()) {
            functionRepository.save(new Function(null, "Thư viện kỹ năng", "SKILL_LIB"));
        }
        // Add more functions as needed
    }

    private void createAdminUser() {
        // Check if admin user already exists by email
        if (userRepository.findByEmail("admin@internhub.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@internhub.com");
            admin.setPassword(passwordEncoder.encode("admin")); // Default password

            // Set ADMIN role
            roleRepository.findByName("ADMIN").ifPresent(admin::setRole);

            // Associate with default department
            departmentRepository.findByName("IT Department").ifPresent(admin::setDepartment);
            
            admin.setIsActive(true); // Admin user is active by default

            userRepository.save(admin);

            // Give admin all permissions to all functions
            Role adminRole = roleRepository.findByName("ADMIN").orElseThrow(() -> new RuntimeException("Admin role not found!"));
            functionRepository.findAll().forEach(function -> {
                RolePermissionId rpId = new RolePermissionId(adminRole.getId(), function.getId());
                if (rolePermissionRepository.findById(rpId).isEmpty()) {
                    RolePermission rp = new RolePermission(rpId, adminRole, function, true, true, true, true);
                    rolePermissionRepository.save(rp);
                }
            });
        }
    }
}
