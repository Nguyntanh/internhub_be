package com.example.internhub_be.service;

import com.example.internhub_be.domain.InternshipProfile;
import com.example.internhub_be.domain.InternshipProfile.InternshipStatus;
import com.example.internhub_be.domain.User;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.AssignSupervisorRequest;
import com.example.internhub_be.payload.InternshipProfileResponse;
import com.example.internhub_be.payload.SupervisorResponse;
import com.example.internhub_be.repository.InternshipProfileRepository;
import com.example.internhub_be.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HRInternshipServiceImpl implements HRInternshipService {

    private final InternshipProfileRepository internshipProfileRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Autowired
    public HRInternshipServiceImpl(InternshipProfileRepository internshipProfileRepository,
                                    UserRepository userRepository,
                                    AuditLogService auditLogService) {
        this.internshipProfileRepository = internshipProfileRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public InternshipProfileResponse assignSupervisors(AssignSupervisorRequest request) {
        InternshipProfile profile = internshipProfileRepository.findById(request.getInternshipProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("InternshipProfile", "id", request.getInternshipProfileId()));

        User oldMentor = profile.getMentor();
        User oldManager = profile.getManager();

        // Gán Mentor nếu có
        if (request.getMentorId() != null) {
            User mentor = userRepository.findById(request.getMentorId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getMentorId()));
            profile.setMentor(mentor);
        }

        // Gán Manager nếu có
        if (request.getManagerId() != null) {
            User manager = userRepository.findById(request.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getManagerId()));
            profile.setManager(manager);
        }

        InternshipProfile updatedProfile = internshipProfileRepository.save(profile);

        // Ghi audit log
        Map<String, Object> details = new HashMap<>();
        details.put("action", "ASSIGN_SUPERVISORS");
        details.put("internship_profile_id", updatedProfile.getId());
        details.put("user_id", updatedProfile.getUser().getId());
        details.put("user_email", updatedProfile.getUser().getEmail());
        
        if (request.getMentorId() != null) {
            details.put("mentor_id", request.getMentorId());
            details.put("old_mentor_id", oldMentor != null ? oldMentor.getId() : null);
        }
        if (request.getManagerId() != null) {
            details.put("manager_id", request.getManagerId());
            details.put("old_manager_id", oldManager != null ? oldManager.getId() : null);
        }
        
        auditLogService.logAction("SUPERVISORS_ASSIGNED", null, details);

        return mapToResponse(updatedProfile);
    }

    @Override
    public Page<InternshipProfileResponse> getInternshipProfiles(String keyword, String status, Pageable pageable) {
        Page<InternshipProfile> profiles;
        
        if (keyword != null && !keyword.isEmpty()) {
            if (status != null && !status.isEmpty()) {
                try {
                    InternshipStatus statusEnum = InternshipStatus.valueOf(status);
                    profiles = internshipProfileRepository.findByUserNameContainingIgnoreCaseAndStatus(keyword, statusEnum, pageable);
                } catch (IllegalArgumentException e) {
                    profiles = internshipProfileRepository.findByUserNameContainingIgnoreCase(keyword, pageable);
                }
            } else {
                profiles = internshipProfileRepository.findByUserNameContainingIgnoreCase(keyword, pageable);
            }
        } else if (status != null && !status.isEmpty()) {
            try {
                InternshipStatus statusEnum = InternshipStatus.valueOf(status);
                profiles = internshipProfileRepository.findByStatus(statusEnum, pageable);
            } catch (IllegalArgumentException e) {
                profiles = internshipProfileRepository.findAll(pageable);
            }
        } else {
            profiles = internshipProfileRepository.findAll(pageable);
        }
        
        return profiles.map(this::mapToResponse);
    }

    @Override
    public InternshipProfileResponse getInternshipProfileById(Long id) {
        InternshipProfile profile = internshipProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InternshipProfile", "id", id));
        return mapToResponse(profile);
    }

    @Override
    public List<SupervisorResponse> getAvailableMentors() {
        List<User> mentors = userRepository.findByRoleName("MENTOR");
        return mentors.stream()
                .map(this::mapToSupervisorResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupervisorResponse> getAvailableManagers() {
        List<User> managers = userRepository.findByRoleName("MANAGER");
        return managers.stream()
                .map(this::mapToSupervisorResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InternshipProfileResponse> getProfilesByMentor(Long mentorId) {
        User mentor = userRepository.findById(mentorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", mentorId));
        return internshipProfileRepository.findByMentor(mentor).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InternshipProfileResponse> getProfilesByManager(Long managerId) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", managerId));
        return internshipProfileRepository.findByManager(manager).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private InternshipProfileResponse mapToResponse(InternshipProfile profile) {
        InternshipProfileResponse response = new InternshipProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setUserName(profile.getUser().getName());
        response.setUserEmail(profile.getUser().getEmail());
        response.setAvatar(profile.getUser().getAvatar());
        
        if (profile.getUniversity() != null) {
            response.setUniversityName(profile.getUniversity().getName());
        }
        
        response.setMajor(profile.getMajor());
        response.setStartDate(profile.getStartDate());
        response.setEndDate(profile.getEndDate());
        response.setStatus(profile.getStatus().name());
        
        if (profile.getPosition() != null) {
            response.setPositionName(profile.getPosition().getName());
            if (profile.getPosition().getDepartment() != null) {
                response.setDepartmentName(profile.getPosition().getDepartment().getName());
            }
        }
        
        if (profile.getMentor() != null) {
            response.setMentorId(profile.getMentor().getId());
            response.setMentorName(profile.getMentor().getName());
            response.setMentorEmail(profile.getMentor().getEmail());
        }
        
        if (profile.getManager() != null) {
            response.setManagerId(profile.getManager().getId());
            response.setManagerName(profile.getManager().getName());
            response.setManagerEmail(profile.getManager().getEmail());
        }
        
        return response;
    }

    private SupervisorResponse mapToSupervisorResponse(User user) {
        SupervisorResponse response = new SupervisorResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        
        if (user.getRole() != null) {
            response.setRoleName(user.getRole().getName());
        }
        
        if (user.getDepartment() != null) {
            response.setDepartmentName(user.getDepartment().getName());
        }
        
        // Đếm số intern đang được gán
        long mentorCount = internshipProfileRepository.countByMentor(user);
        long managerCount = internshipProfileRepository.countByManager(user);
        response.setAssignedInternCount(mentorCount + managerCount);
        
        return response;
    }
}
