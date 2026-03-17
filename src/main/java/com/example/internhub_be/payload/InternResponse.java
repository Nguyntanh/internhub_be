package com.example.internhub_be.payload;

import com.example.internhub_be.domain.InternshipProfile.InternshipStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class InternResponse {

    // internship_profiles.id  (FE dùng làm key)
    private Long   id;

    // users fields
    private Long   userId;
    private String fullName;   // FE dùng intern.fullName
    private String email;
    private String phone;
    private String avatar;

    // internship_profiles fields
    private String    major;
    private LocalDate startDate;
    private LocalDate endDate;
    private InternshipStatus status;

    // university
    private Long   universityId;
    private String universityName;

    // position + department (FE cần: intern.posName, intern.departmentId, intern.departmentName)
    private Long   positionId;
    private String positionName;   // FE: intern.posName
    private Long   departmentId;
    private String departmentName; // FE: intern.departmentName

    // mentor / manager (FE hiện chỉ dùng id để pre-fill form edit)
    private Long   mentorId;
    private String mentorName;
    private Long   managerId;
    private String managerName;
}
