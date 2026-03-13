package com.example.internhub_be.service;

import com.example.internhub_be.domain.InternshipProfile;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.payload.ChangePasswordRequest;
import com.example.internhub_be.payload.UserProfileResponse;
import com.example.internhub_be.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;


    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditLogService auditLogService, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findUserWithProfileByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        UserProfileResponse response = new UserProfileResponse();
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAvatar(user.getAvatar());

        // Set department name from user's department
        response.setDepartmentName(user.getDepartment() != null ? user.getDepartment().getName() : null);

        // Handle internship profile if it exists
        InternshipProfile ip = user.getInternshipProfile();
        if (ip != null) {
            response.setMajor(ip.getMajor());
            response.setUniversityName(ip.getUniversity() != null ? ip.getUniversity().getName() : null);
            response.setPositionName(ip.getPosition() != null ? ip.getPosition().getName() : null);
            response.setStatus(ip.getStatus() != null ? ip.getStatus().name() : null);
            response.setStartDate(ip.getStartDate());
            response.setEndDate(ip.getEndDate());
            response.setMentorName(ip.getMentor() != null ? ip.getMentor().getName() : null);

            // Calculate daysRemaining
            if (ip.getEndDate() != null) {
                LocalDate today = LocalDate.now();
                if (ip.getEndDate().isBefore(today)) {
                    response.setDaysRemaining(0L); // Internship ended
                } else {
                    response.setDaysRemaining(ChronoUnit.DAYS.between(today, ip.getEndDate()));
                }
            } else {
                response.setDaysRemaining(null);
            }
        } else {
            // If no internship profile, explicitly set fields to null and daysRemaining to null
            response.setMajor(null);
            response.setUniversityName(null);
            response.setPositionName(null);
            response.setStatus(null);
            response.setStartDate(null);
            response.setEndDate(null);
            response.setMentorName(null);
            response.setDaysRemaining(null);
        }

        return response;
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest changePasswordRequest) {
        // Retrieve the authenticated user's email from SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentAuthenticatedUserEmail = authentication.getName();

        if (!currentAuthenticatedUserEmail.equals(email)) {
            throw new BadCredentialsException("Cannot change password for another user.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // So khớp mật khẩu cũ
        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Incorrect old password.");
        }

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);

        // Ghi Nhật ký Hệ thống (Audit Log)
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now().toString()); // Storing as String for consistent JSON representation

        // Call logAction with the correct parameters
        auditLogService.logAction("CHANGE_PASSWORD", user, details);
    }
}