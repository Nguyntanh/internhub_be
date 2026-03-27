package com.example.internhub_be.payload.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class RadarAnalyticsResponse {

    private Long   internId;
    private String internName;
    private String internEmail;
    private String phone;

    private String universityName;
    private String major;
    private String positionName;
    private String departmentName;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;

    private Long   mentorId;
    private String mentorName;
    private Long   managerId;
    private String managerName;

    private List<SkillScore> skillScores;
    private List<BenchmarkScore> benchmarkScores;

    private Integer    totalTasksReviewed;
    private Integer    totalTasksAll;
    private BigDecimal overallScore;

    @Data
    @Builder
    public static class SkillScore {
        private Long       skillId;
        private String     skillName;
        private Long       parentSkillId;
        private String     parentSkillName;
        private BigDecimal score;
        private Integer    totalWeight;
        private Integer    taskCount;
    }

    @Data
    @Builder
    public static class BenchmarkScore {
        private Long       skillId;
        private String     skillName;
        private BigDecimal benchmarkScore;
    }
}