package com.example.internhub_be.controller;

import com.example.internhub_be.payload.AssignSupervisorRequest;
import com.example.internhub_be.payload.InternshipProfileResponse;
import com.example.internhub_be.payload.SupervisorResponse;
import com.example.internhub_be.service.HRInternshipService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/interns")
@PreAuthorize("hasAnyRole('HR', 'ADMIN')")
public class HRController {

    private final HRInternshipService hrInternshipService;

    @Autowired
    public HRController(HRInternshipService hrInternshipService) {
        this.hrInternshipService = hrInternshipService;
    }

    /**
     * Gán Mentor và Manager cho Internship Profile
     */
    @PostMapping("/assign-supervisors")
    public ResponseEntity<InternshipProfileResponse> assignSupervisors(
            @Valid @RequestBody AssignSupervisorRequest request) {
        InternshipProfileResponse response = hrInternshipService.assignSupervisors(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách tất cả Internship Profiles có phân trang
     */
    @GetMapping
    public ResponseEntity<Page<InternshipProfileResponse>> getInternshipProfiles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<InternshipProfileResponse> profiles = hrInternshipService.getInternshipProfiles(keyword, status, pageable);
        return ResponseEntity.ok(profiles);
    }

    /**
     * Lấy chi tiết Internship Profile theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<InternshipProfileResponse> getInternshipProfileById(@PathVariable Long id) {
        InternshipProfileResponse response = hrInternshipService.getInternshipProfileById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách Mentors có sẵn để gán
     */
    @GetMapping("/mentors/available")
    public ResponseEntity<List<SupervisorResponse>> getAvailableMentors() {
        List<SupervisorResponse> mentors = hrInternshipService.getAvailableMentors();
        return ResponseEntity.ok(mentors);
    }

    /**
     * Lấy danh sách Managers có sẵn để gán
     */
    @GetMapping("/managers/available")
    public ResponseEntity<List<SupervisorResponse>> getAvailableManagers() {
        List<SupervisorResponse> managers = hrInternshipService.getAvailableManagers();
        return ResponseEntity.ok(managers);
    }

    /**
     * Lấy danh sách Internship Profiles của một Mentor cụ thể
     */
    @GetMapping("/by-mentor/{mentorId}")
    public ResponseEntity<List<InternshipProfileResponse>> getProfilesByMentor(@PathVariable Long mentorId) {
        List<InternshipProfileResponse> profiles = hrInternshipService.getProfilesByMentor(mentorId);
        return ResponseEntity.ok(profiles);
    }

    /**
     * Lấy danh sách Internship Profiles của một Manager cụ thể
     */
    @GetMapping("/by-manager/{managerId}")
    public ResponseEntity<List<InternshipProfileResponse>> getProfilesByManager(@PathVariable Long managerId) {
        List<InternshipProfileResponse> profiles = hrInternshipService.getProfilesByManager(managerId);
        return ResponseEntity.ok(profiles);
    }
}
