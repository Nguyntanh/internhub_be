package com.example.internhub_be.payload.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateMicroTaskRequest {

    private String title;

    private String description;

    private LocalDateTime deadline;

    @NotEmpty(message = "Intern list cannot be empty")
    private List<Long> internIds;

    @NotEmpty(message = "Skill list cannot be empty")
    private List<SkillWeightRequest> skills;
}