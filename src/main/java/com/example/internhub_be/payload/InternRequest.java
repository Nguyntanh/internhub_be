package com.example.internhub_be.payload;

import com.example.internhub_be.domain.InternshipProfile.InternshipStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class InternRequest {
    private Long userId;

    // ── users table ───────────────────────────────────────────────────────
    private String fullName;   // → users.name
    private String email;      // → users.email
    private String phone;      // → users.phone

    // ── internship_profiles table ────────────────────────────────────────
    private String    major;
    private Long      universityId;
    private Long      positionId;
    private Long      departmentId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long      mentorId;
    private Long      managerId;
    private InternshipStatus status;
}
