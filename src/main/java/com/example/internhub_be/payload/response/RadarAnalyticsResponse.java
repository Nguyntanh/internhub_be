package com.example.internhub_be.payload.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response cho Radar Analytics — Năng lực (SkillCore) vs Benchmark.
 *
 * Role access:
 *   ADMIN / HR  : Full
 *   MANAGER     : View (intern trong phòng ban của mình)
 *   MENTOR      : View (intern mình phụ trách)
 *   INTERN      : Self (chỉ xem của bản thân)
 */
@Data
@Builder
public class RadarAnalyticsResponse {

    // ── Thông tin intern ───────────────────────────────────────────────────
    private Long   internId;
    private String internName;
    private String internEmail;
    private String positionName;
    private String departmentName;

    // ── Điểm kỹ năng thực tế (từ task_skill_ratings đã Reviewed) ──────────
    private List<SkillScore> skillScores;

    // ── Điểm benchmark tiêu chuẩn ─────────────────────────────────────────
    private List<BenchmarkScore> benchmarkScores;

    // ── Thống kê nhanh ────────────────────────────────────────────────────
    private Integer    totalTasksReviewed;
    private Integer    totalTasksAll;
    private BigDecimal overallScore;   // Trung bình có trọng số toàn bộ kỹ năng (0–10)

    // ─── Inner DTOs ───────────────────────────────────────────────────────

    @Data
    @Builder
    public static class SkillScore {
        private Long       skillId;
        private String     skillName;
        private Long       parentSkillId;    // null nếu là skill gốc
        private String     parentSkillName;
        private BigDecimal score;            // 0–10, trung bình có trọng số
        private Integer    totalWeight;
        private Integer    taskCount;
    }

    @Data
    @Builder
    public static class BenchmarkScore {
        private Long       skillId;
        private String     skillName;
        private BigDecimal benchmarkScore;   // Mặc định 7.0, mở rộng sau theo vị trí
    }
}