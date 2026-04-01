package com.example.internhub_be.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternDashboardResponse {
    // Overview
    private Long userId;
    private String internName;
    private String positionName;
    private String mentorName;
    private Long daysRemaining;

    // Target Skills
    private List<SkillResponse> targetSkills;

    // Tasks (Open or In_Progress)
    private List<TaskResponse> tasks;

    // Roadmap
    private List<MilestoneResponse> roadmap;
}