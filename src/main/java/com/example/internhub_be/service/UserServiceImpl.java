package com.example.internhub_be.service;

import com.example.internhub_be.domain.InternshipProfile;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.exception.InvalidFileException;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.ChangePasswordRequest;
import com.example.internhub_be.payload.NewAvatarUrlResponse;
import com.example.internhub_be.payload.UserProfileResponse;
import com.example.internhub_be.payload.UserResponse;
import com.example.internhub_be.repository.InternshipProfileRepository;
import com.example.internhub_be.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;
    private final InternshipProfileRepository internshipProfileRepository; // THÊM
    private final Path uploadPath;

    private static final String DEFAULT_AVATAR = "default_avatar.png";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png"};

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService,
            ObjectMapper objectMapper,
            InternshipProfileRepository internshipProfileRepository, // THÊM
            @Value("${app.upload.dir:uploads}") String uploadDir
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
        this.internshipProfileRepository = internshipProfileRepository; // THÊM
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

            if (ip.getEndDate() != null) {
                LocalDate today = LocalDate.now();
                if (ip.getEndDate().isBefore(today)) {
                    response.setDaysRemaining(0L);
                } else {
                    response.setDaysRemaining(ChronoUnit.DAYS.between(today, ip.getEndDate()));
                }
            } else {
                response.setDaysRemaining(null);
            }
        } else {
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
            throw new InvalidFileException("File size exceeds the limit of " + MAX_FILE_SIZE / (1024 * 1024) + "MB");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = Optional.ofNullable(originalFilename)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(f.lastIndexOf(".") + 1).toLowerCase())
                .orElseThrow(() -> new InvalidFileException("Could not determine file extension."));

        if (!Arrays.asList(ALLOWED_EXTENSIONS).contains(fileExtension)) {
            throw new InvalidFileException("Only " + String.join(", ", ALLOWED_EXTENSIONS) + " files are allowed.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        String oldAvatarUrl = user.getAvatar();
        if (oldAvatarUrl != null && !oldAvatarUrl.contains(DEFAULT_AVATAR)) {
            try {
                String oldFilename = oldAvatarUrl.substring(oldAvatarUrl.lastIndexOf("/") + 1);
                Path oldFilePath = this.uploadPath.resolve(oldFilename).normalize();
                if (Files.exists(oldFilePath)) {
                    Files.delete(oldFilePath);
                }
            } catch (IOException e) {
                System.err.println("Could not delete old avatar file: " + e.getMessage());
            }
        }

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

        Map<String, Object> details = new HashMap<>();
        details.put("timestamp", LocalDateTime.now().toString());
        details.put("newAvatarUrl", newAvatarUrl);
        details.put("oldAvatarUrl", oldAvatarUrl);
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

    // THÊM MỚI: lấy user có role INTERN chưa có hồ sơ thực tập
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAvailableInterns() {
        Set<Long> userIdsWithProfile = internshipProfileRepository.findAll()
                .stream()
                .map(ip -> ip.getUser().getId())
                .collect(Collectors.toSet());

        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != null
                        && "INTERN".equalsIgnoreCase(u.getRole().getName())
                        && !userIdsWithProfile.contains(u.getId()))
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse resp = new UserResponse();
        resp.setId(user.getId());
        resp.setName(user.getName());
        resp.setEmail(user.getEmail());
        resp.setIsActive(user.getIsActive());
        resp.setPhone(user.getPhone());
        resp.setAvatar(user.getAvatar());
        resp.setCreatedAt(user.getCreatedAt());
        if (user.getRole() != null) {
            resp.setRoleId(user.getRole().getId());
            resp.setRoleName(user.getRole().getName());
        }
        if (user.getDepartment() != null) {
            resp.setDepartmentId(user.getDepartment().getId());
            resp.setDepartmentName(user.getDepartment().getName());
        }
        return resp;
    }
}