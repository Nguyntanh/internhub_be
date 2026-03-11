package com.example.internhub_be.service;

import com.example.internhub_be.payload.AssignSupervisorRequest;
import com.example.internhub_be.payload.InternshipProfileResponse;
import com.example.internhub_be.payload.SupervisorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HRInternshipService {

    /**
     * Gán Mentor và Manager cho Internship Profile
     */
    InternshipProfileResponse assignSupervisors(AssignSupervisorRequest request);

    /**
     * Lấy danh sách Internship Profiles có phân trang và filter
     */
    Page<InternshipProfileResponse> getInternshipProfiles(String keyword, String status, Pageable pageable);

    /**
     * Lấy chi tiết Internship Profile theo ID
     */
    InternshipProfileResponse getInternshipProfileById(Long id);

    /**
     * Lấy danh sách available Mentors (có role phù hợp)
     */
    List<SupervisorResponse> getAvailableMentors();

    /**
     * Lấy danh sách available Managers (có role phù hợp)
     */
    List<SupervisorResponse> getAvailableManagers();

    /**
     * Lấy danh sách Internship Profiles của một Mentor cụ thể
     */
    List<InternshipProfileResponse> getProfilesByMentor(Long mentorId);

    /**
     * Lấy danh sách Internship Profiles của một Manager cụ thể
     */
    List<InternshipProfileResponse> getProfilesByManager(Long managerId);
}
