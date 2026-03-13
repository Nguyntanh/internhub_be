package com.example.internhub_be.payload.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FinalEvaluationResponse {

    private Long id;
    private Long internId;
    private String internName;
    private String internEmail;
    private Long mentorId;
    private String mentorName;
    private String overallComment;
    private String status;
    private Boolean isLocked;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;

    // Bảng điểm tổng hợp từ các Micro-tasks
    private List<SkillSummary> skillSummaries;
    private Integer totalTasksReviewed;
    private Integer totalTasksAll;

    @Data
    public static class SkillSummary {
        private Long skillId;
        private String skillName;
        private BigDecimal averageScore;   // Trung bình có trọng số
        private Integer totalWeight;
        private Integer taskCount;
    }
}
