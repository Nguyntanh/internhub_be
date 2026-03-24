package com.example.internhub_be.service;

import com.example.internhub_be.domain.InternshipMilestone;
import com.example.internhub_be.domain.InternshipProfile;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.exception.InvalidFileException;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.ChangePasswordRequest;
import com.example.internhub_be.payload.NewAvatarUrlResponse;
import com.example.internhub_be.payload.UserProfileResponse;
import com.example.internhub_be.payload.UserResponse;
import com.example.internhub_be.repository.UserRepository;
import com.example.internhub_be.repository.InternshipMilestoneRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final Path uploadPath;
    private final InternshipMilestoneRepository milestoneRepository;

    private static final String DEFAULT_AVATAR = "default_avatar.png";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png"};

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService,
            ObjectMapper objectMapper,
            InternshipMilestoneRepository milestoneRepository,
            @Value("${app.upload.dir:uploads}") String uploadDir
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
        this.milestoneRepository = milestoneRepository;
        this.uploadPath = Paths.get(uploadDir, "avatars").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
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
        response.setDepartmentName(user.getDepartment() != null ? user.getDepartment().getName() : null);

        InternshipProfile ip = user.getInternshipProfile();
        if (ip != null) {
            response.setMajor(ip.getMajor());
            response.setUniversityName(ip.getUniversity() != null ? ip.getUniversity().getName() : null);
            response.setPositionName(ip.getPosition() != null ? ip.getPosition().getName() : null);
            response.setStatus(ip.getStatus() != null ? ip.getStatus().name() : null);
            response.setStartDate(ip.getStartDate());
            response.setEndDate(ip.getEndDate());
            response.setMentorName(ip.getMentor() != null ? ip.getMentor().getName() : null);

            // 1. Tính toán ngày thực tập còn lại
            if (ip.getEndDate() != null) {
                LocalDate today = LocalDate.now();
                long remaining = ChronoUnit.DAYS.between(today, ip.getEndDate());
                response.setDaysRemaining(Math.max(0L, remaining));
            }

            // 2. Tính toán Roadmap Milestones dựa trên Position
            // if (ip.getPosition() != null && ip.getStartDate() != null) {
            //     List<InternshipMilestone> milestones = milestoneRepository
            //             .findByPositionIdOrderByOrderIndexAsc(ip.getPosition().getId());
                
            //     response.setRoadmap(calculateRoadmap(milestones, ip.getStartDate()));
            // } else {
            //     response.setRoadmap(new ArrayList<>());
            // }
        } else {
            // Trường hợp không có profile
            response.setDaysRemaining(null);
            response.setRoadmap(new ArrayList<>());
        }

        return response;
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordRequest changePasswordRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentAuthenticatedUserEmail = authentication.getName();

        if (!currentAuthenticatedUserEmail.equals(email)) {
            throw new BadCredentialsException("Cannot change password for another user.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Incorrect old password.");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        userRepository.save(user);

        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now().toString());
        auditLogService.logAction("CHANGE_PASSWORD", user, details);
    }

    @Override
    @Transactional
    public NewAvatarUrlResponse updateAvatar(String email, MultipartFile file) {
        if (file.isEmpty()) {
            throw new InvalidFileException("Cannot upload empty file.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("File size exceeds the limit of 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = Optional.ofNullable(originalFilename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".") + 1).toLowerCase())
                .orElseThrow(() -> new InvalidFileException("Could not determine file extension."));

        if (!Arrays.asList(ALLOWED_EXTENSIONS).contains(fileExtension)) {
            throw new InvalidFileException("Only jpg, jpeg, png files are allowed.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Xóa avatar cũ
        String oldAvatarUrl = user.getAvatar();
        if (oldAvatarUrl != null && !oldAvatarUrl.contains(DEFAULT_AVATAR)) {
            try {
                String oldFilename = oldAvatarUrl.substring(oldAvatarUrl.lastIndexOf("/") + 1);
                Path oldFilePath = this.uploadPath.resolve(oldFilename).normalize();
                Files.deleteIfExists(oldFilePath);
            } catch (IOException e) {
                // Log error but continue
            }
        }

        // Lưu avatar mới
        String newFilename = "avatar_" + user.getId() + "_" + System.currentTimeMillis() + "." + fileExtension;
        Path targetLocation = this.uploadPath.resolve(newFilename);

        try {
            Files.copy(file.getInputStream(), targetLocation);
        } catch (IOException e) {
            throw new InvalidFileException("Could not store file " + newFilename, e);
        }

        String newAvatarUrl = "/assets/avatars/" + newFilename;
        user.setAvatar(newAvatarUrl);
        userRepository.save(user);

        // Ghi log
        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now().toString());
        details.put("newAvatarUrl", newAvatarUrl);
        auditLogService.logAction("UPDATE_AVATAR", user, details);

        return new NewAvatarUrlResponse(newAvatarUrl);
    }

    @Override
    public List<User> getUsersByRole(String roleName) {

        return userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() != null &&
                        roleName.equalsIgnoreCase(u.getRole().getName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getInterns() {
        return getUsersByRole("INTERN").stream()
                .map(this::mapUserToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRoleResponse(String roleName) {
        return getUsersByRole(roleName).stream()
                .map(this::mapUserToUserResponse)
                .collect(Collectors.toList());
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