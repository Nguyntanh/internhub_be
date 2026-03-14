package com.example.internhub_be.payload.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateMicroTaskRequest {

    private String title;

    private String description;

    private LocalDateTime deadline;

    private List<Long> internIds;

    private List<SkillWeightRequest> skills;
}