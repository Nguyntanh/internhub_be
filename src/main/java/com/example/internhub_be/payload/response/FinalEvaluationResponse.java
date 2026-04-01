package com.example.internhub_be.payload.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FinalEvaluationResponse {

    // ── Identity ──────────────────────────────────────────────────────────────
    private Long   id;
    private Long   internId;
    private String internName;
    private String internEmail;
    private Long   mentorId;
    private String mentorName;

    // ── Evaluation content ────────────────────────────────────────────────────
    private String        overallComment;
    /** EvaluationStatus enum name: "DRAFT" | "SUBMITTED" */
    private String        status;
    private Boolean       isLocked;
    private LocalDateTime submittedAt;
    private LocalDateTime createdAt;

    // ── Task summary ──────────────────────────────────────────────────────────
    private int totalTasksAll;
    private int totalTasksReviewed;

    // ── Skill summaries ───────────────────────────────────────────────────────
    private List<SkillSummary> skillSummaries;

    // ── Inner DTO ─────────────────────────────────────────────────────────────
    @Data
    public static class SkillSummary {
        private Long       skillId;
        private String     skillName;
        private int        totalWeight;
        private int        taskCount;
        private BigDecimal averageScore;
    }
}