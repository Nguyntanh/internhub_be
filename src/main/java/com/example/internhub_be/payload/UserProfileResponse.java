package com.example.internhub_be.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.example.internhub_be.payload.response.MilestoneResponse;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private String name;
    private String email;
    private String phone;
    private String avatar;
    private String departmentName; // New field

    // InternshipProfile fields (can be null)
    private String major;
    private String universityName;
    private String positionName;
    private String status; // Assuming InternshipProfile.status is a String or can be represented as one
    private LocalDate startDate;
    private LocalDate endDate;
    private String mentorName;
    private Long daysRemaining; // New field
    private List<MilestoneResponse> roadmap;
}
