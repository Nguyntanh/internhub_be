package com.example.internhub_be.payload.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InternDashboardResponse {
    // Overview
    private String positionName;
    private String mentorName;
    private Long daysRemaining; // Number of days remaining until the end date of the internship

    // Target Skills - Note: This currently assumes a direct link from InternshipPosition to Skill.
    // If not, this section needs clarification on how target skills are determined for an InternshipPosition.
    private List<SkillResponse> targetSkills;

    // Tasks (Open or In_Progress)
    private List<TaskResponse> tasks;

    // Roadmap
    private List<MilestoneResponse> roadmap;
}
