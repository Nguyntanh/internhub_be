package com.example.internhub_be.payload.response;

import com.example.internhub_be.domain.InternshipDecision;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Báo cáo tổng hợp dành cho Manager xem trước khi ra quyết định.
 *
 * Bao gồm:
 *   - Thông tin intern + hồ sơ thực tập
 *   - Nhận xét của Mentor (FinalEvaluation)
 *   - Tổng điểm kỹ năng (từ task_skill_ratings)
 *   - Lịch sử thực thi (các task đã hoàn thành)
 *   - Quyết định hiện tại (nếu đã có)
 */
@Data
@Builder
public class ManagerReviewResponse {

    // ── Thông tin intern ──────────────────────────────────────────────────
    private Long   internId;
    private String internName;
    private String internEmail;
    private String internPhone;
    private String internAvatar;
    private String universityName;
    private String major;

    // ── Hồ sơ thực tập ───────────────────────────────────────────────────
    private Long      internshipProfileId;
    private String    positionName;
    private String    departmentName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String    internshipStatus;   // In_Progress, Completed, v.v.

    // ── Thông tin Mentor ──────────────────────────────────────────────────
    private Long   mentorId;
    private String mentorName;
    private String mentorEmail;

    // ── Đánh giá của Mentor (FinalEvaluation) ────────────────────────────
    private Long          evaluationId;
    private String        overallComment;       // Nhận xét tổng kết của Mentor
    private String        evaluationStatus;     // DRAFT / SUBMITTED
    private LocalDateTime evaluationSubmittedAt;

    // ── Tổng điểm kỹ năng ────────────────────────────────────────────────
    private List<SkillSummaryItem> skillSummaries;
    private BigDecimal             overallScore;       // Điểm trung bình tổng (0–10)
    private Integer                totalTasksAll;
    private Integer                totalTasksReviewed;

    // ── Lịch sử thực thi (task đã Reviewed) ──────────────────────────────
    private List<TaskHistoryItem> taskHistory;

    // ── Quyết định hiện tại (null nếu chưa có) ───────────────────────────
    private DecisionInfo currentDecision;

    // ─── Inner DTOs ───────────────────────────────────────────────────────

    @Data
    @Builder
    public static class SkillSummaryItem {
        private Long       skillId;
        private String     skillName;
        private String     parentSkillName;
        private BigDecimal averageScore;
        private Integer    totalWeight;
        private Integer    taskCount;
    }

    @Data
    @Builder
    public static class TaskHistoryItem {
        private Long          taskId;
        private String        title;
        private String        description;
        private String        status;
        private LocalDateTime deadline;
        private LocalDateTime createdAt;
        /** Điểm kỹ năng của task này */
        private List<TaskSkillItem> skills;
    }

    @Data
    @Builder
    public static class TaskSkillItem {
        private String     skillName;
        private Integer    weight;
        private BigDecimal ratingScore;
        private String     reviewComment;
    }

    @Data
    @Builder
    public static class DecisionInfo {
        private Long                         decisionId;
        private InternshipDecision.DecisionType decision;
        private String                       managerComment;
        private String                       managerName;
        private LocalDateTime                createdAt;
        private Boolean                      hrNotified;
    }
}