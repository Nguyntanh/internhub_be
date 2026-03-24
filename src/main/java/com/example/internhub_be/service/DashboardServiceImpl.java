package com.example.internhub_be.service;

import com.example.internhub_be.domain.InternshipProfile;
import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.response.InternDashboardResponse;
import com.example.internhub_be.payload.response.MilestoneResponse;
import com.example.internhub_be.payload.response.SkillResponse;
import com.example.internhub_be.payload.response.TaskResponse;
import com.example.internhub_be.repository.InternshipMilestoneRepository;
import com.example.internhub_be.repository.InternshipProfileRepository;
import com.example.internhub_be.repository.MicroTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final InternshipProfileRepository internshipProfileRepository;
    private final MicroTaskRepository microTaskRepository;
    private final InternshipMilestoneRepository internshipMilestoneRepository;

    @Override
    public InternDashboardResponse getInternDashboard(Long internId) {
        InternshipProfile internProfile = internshipProfileRepository.findByUserId(internId)
                .orElseThrow(() -> new ResourceNotFoundException("InternshipProfile", "internId", internId));

        // Overview
        String positionName = internProfile.getPosition() != null ? internProfile.getPosition().getName() : "N/A";
        String mentorName = internProfile.getMentor() != null ? internProfile.getMentor().getName() : "N/A";

        long daysRemaining = 0;
        if (internProfile.getEndDate() != null) {
            // Calculate days remaining from today to the end date of the internship
            daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), internProfile.getEndDate());
            if (daysRemaining < 0) { // If end date has passed
                daysRemaining = 0;
            }
        }

        // Target Skills - As discussed, InternshipPosition does not directly link to skills.
        // This section needs clarification on how target skills are determined for an InternshipPosition.
        // For now, it returns an empty list.
        List<SkillResponse> targetSkills = Collections.emptyList();
        // Example if InternshipPosition had a getSkills() method:
        // List<SkillResponse> targetSkills = internProfile.getPosition().getSkills().stream()
        //         .map(skill -> SkillResponse.builder().id(skill.getId()).name(skill.getName()).build())
        //         .collect(Collectors.toList());


        // Tasks (Open or In_Progress)
        List<MicroTask.MicroTaskStatus> desiredStatuses = Arrays.asList(MicroTask.MicroTaskStatus.Todo, MicroTask.MicroTaskStatus.In_Progress);
        List<TaskResponse> tasks = microTaskRepository.findByInternIdAndStatusIn(internId, desiredStatuses).stream()
                .map(task -> TaskResponse.builder()
                        .id(task.getId())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .status(task.getStatus().name())
                        .deadline(task.getDeadline())
                        .build())
                .collect(Collectors.toList());

        // Roadmap
        List<MilestoneResponse> roadmap = Collections.emptyList();
        if (internProfile.getPosition() != null) {
            roadmap = internshipMilestoneRepository.findByPositionIdOrderByOrderIndexAsc(internProfile.getPosition().getId()).stream()
                    .map(milestone -> MilestoneResponse.builder()
                            .id(milestone.getId())
                            .title(milestone.getTitle())
                            .description(milestone.getDescription())
                            .orderIndex(milestone.getOrderIndex())
                            // Status for milestone is not directly available in entity, omitting for now.
                            .build())
                    .collect(Collectors.toList());
        }


        return InternDashboardResponse.builder()
                .positionName(positionName)
                .mentorName(mentorName)
                .daysRemaining(daysRemaining)
                .targetSkills(targetSkills)
                .tasks(tasks)
                .roadmap(roadmap)
                .build();
    }
}
