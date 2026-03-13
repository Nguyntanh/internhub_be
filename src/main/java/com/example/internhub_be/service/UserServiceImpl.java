package com.example.internhub_be.service;

import com.example.internhub_be.domain.InternshipProfile;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.payload.UserProfileResponse;
import com.example.internhub_be.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}