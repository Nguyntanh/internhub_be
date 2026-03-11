package com.example.internhub_be.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssignSupervisorRequest {

    @NotNull(message = "ID của Internship Profile không được để trống")
    private Long internshipProfileId;

    private Long mentorId;

    private Long managerId;
}
