package com.example.internhub_be.payload;

import lombok.Data;
import java.time.LocalDate;

@Data
public class InternshipProfileResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private String avatar;
    private String universityName;
    private String major;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String positionName;
    private String departmentName;
    private Long mentorId;
    private String mentorName;
    private String mentorEmail;
    private Long managerId;
    private String managerName;
    private String managerEmail;
}
