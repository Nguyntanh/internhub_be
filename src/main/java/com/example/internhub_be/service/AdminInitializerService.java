package com.example.internhub_be.service;

import com.example.internhub_be.domain.Department;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.domain.User.UserRole; // Import internal enum
import com.example.internhub_be.repository.DepartmentRepository;
import com.example.internhub_be.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct; // For @PostConstruct

@Service
public class AdminInitializerService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializerService(UserRepository userRepository, DepartmentRepository departmentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        createDefaultDepartment();
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

    private void createAdminUser() {
        // Check if admin user already exists by email
        if (userRepository.findByEmail("admin@internhub.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@internhub.com");
            admin.setPassword(passwordEncoder.encode("admin")); // Default password
            admin.setRole(UserRole.ADMIN); // Use the internal enum
            
            // Associate with default department
            departmentRepository.findByName("IT Department").ifPresent(admin::setDepartment);
            
            userRepository.save(admin);
        }
    }
}
