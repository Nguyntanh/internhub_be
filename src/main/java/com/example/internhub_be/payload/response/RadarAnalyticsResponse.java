package com.example.internhub_be.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RadarAnalyticsResponse {

    // ── Intern identity ───────────────────────────────────────────────────────
    private Long   internId;
    private String internName;
    private String internEmail;
    private String phone;

    // ── Profile info ──────────────────────────────────────────────────────────
    private String    universityName;
    private String    major;
    private String    positionName;
    private String    departmentName;
    private String    status;
    private LocalDate startDate;
    private LocalDate endDate;

    // ── Mentor / Manager ──────────────────────────────────────────────────────
    private Long   mentorId;
    private String mentorName;
    private Long   managerId;
    private String managerName;

    // ── Skill scores ──────────────────────────────────────────────────────────
    private List<SkillScore>     skillScores;
    private List<BenchmarkScore> benchmarkScores;

    // ── Summary ───────────────────────────────────────────────────────────────
    private int        totalTasksAll;
    private int        totalTasksReviewed;
    private BigDecimal overallScore;

    // ── Inner DTOs ────────────────────────────────────────────────────────────

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillScore {
        private Long       skillId;
        private String     skillName;
        private Long       parentSkillId;
        private String     parentSkillName;
        private BigDecimal score;
        private int        totalWeight;
        private int        taskCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenchmarkScore {
        private Long       skillId;
        private String     skillName;
        private BigDecimal benchmarkScore;
    }
}