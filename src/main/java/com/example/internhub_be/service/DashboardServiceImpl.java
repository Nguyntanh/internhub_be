package com.example.internhub_be.service;

import com.example.internhub_be.domain.FinalEvaluation;
import com.example.internhub_be.domain.InternshipProfile;
import com.example.internhub_be.domain.MicroTask;
import com.example.internhub_be.exception.ResourceNotFoundException;
import com.example.internhub_be.payload.response.*;
import com.example.internhub_be.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final InternshipProfileRepository internshipProfileRepository;
    private final MicroTaskRepository microTaskRepository;
    private final InternshipMilestoneRepository internshipMilestoneRepository;
    private final FinalEvaluationRepository finalEvaluationRepository;

    @Override
    public InternDashboardResponse getInternDashboard(Long userId) {
        InternshipProfile internProfile = internshipProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("InternshipProfile", "userId", userId));

        // Overview
        String positionName = internProfile.getPosition() != null 
                ? internProfile.getPosition().getName() : "N/A";
        String mentorName = internProfile.getMentor() != null 
                ? internProfile.getMentor().getName() : "N/A";

        long daysRemaining = 0;
        if (internProfile.getEndDate() != null) {
            daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), internProfile.getEndDate());
            daysRemaining = Math.max(0, daysRemaining);
        }

        // Tasks cho Intern Dashboard (Đang làm)
        List<MicroTask.MicroTaskStatus> activeStatuses = Arrays.asList(
                MicroTask.MicroTaskStatus.Todo,
                MicroTask.MicroTaskStatus.In_Progress
        );

        List<TaskResponse> tasks = microTaskRepository.findByInternIdAndStatusIn(userId, activeStatuses).stream()
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
                    .map(m -> MilestoneResponse.builder()
                            .id(m.getId())
                            .title(m.getTitle())
                            .description(m.getDescription())
                            .orderIndex(m.getOrderIndex())
                            .build())
                    .collect(Collectors.toList());
        }

        return InternDashboardResponse.builder()
                .userId(userId)
                .internName(internProfile.getUser().getName())
                .positionName(positionName)
                .mentorName(mentorName)
                .daysRemaining(daysRemaining)
                .targetSkills(Collections.emptyList())
                .tasks(tasks)
                .roadmap(roadmap)
                .build();
    }

    @Override
    public List<ManagerInternSummaryResponse> getManagerDashboard() {
        // Tối ưu: Lấy tất cả Profile. Lưu ý: Nên dùng JPA Fetch Join trong Repository để tránh N+1
        List<InternshipProfile> profiles = internshipProfileRepository.findAll();

        return profiles.stream().map(profile -> {
            Long userId = profile.getUser().getId();

            // 1. Tính toán tỷ lệ hoàn thành (Dựa trên status 'Reviewed' trong DB)
            List<MicroTask> tasks = microTaskRepository.findByInternId(userId);
            long totalTasks = tasks.size();
            long completedTasks = tasks.stream()
                    .filter(t -> t.getStatus() == MicroTask.MicroTaskStatus.Reviewed)
                    .count();
            double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0.0;

            // 2. Lấy kết quả đánh giá (GPA chốt)
            FinalEvaluation evaluation = finalEvaluationRepository.findByInternId(userId)
                    .orElse(null);

            return ManagerInternSummaryResponse.builder()
                    .internId(userId)
                    .fullName(profile.getUser().getName())
                    .positionName(profile.getPosition() != null ? profile.getPosition().getName() : "N/A")
                    .gpa(evaluation != null && evaluation.getGrade() != null ? evaluation.getGrade().doubleValue() : 0.0)
                    .status(evaluation != null ? evaluation.getStatus().name() : "IN_PROGRESS")
                    .completionRate(Math.round(completionRate * 100.0) / 100.0)
                    .build();
        })
        .sorted((a, b) -> b.getGpa().compareTo(a.getGpa())) // Sắp xếp giảm dần theo GPA
        .collect(Collectors.toList());
    }
}