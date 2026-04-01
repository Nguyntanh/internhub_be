package com.example.internhub_be.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Lưu quyết định cuối của Manager (CEO) về kết quả thực tập.
 *
 * Quan hệ:
 *   - internshipProfile : 1 profile có tối đa 1 quyết định (unique)
 *   - manager           : người đưa ra quyết định
 *   - finalEvaluation   : bản đánh giá đã SUBMITTED của Mentor
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "internship_decisions")
public class InternshipDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Liên kết hồ sơ thực tập ──────────────────────────────────────────
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "internship_profile_id", unique = true, nullable = false)
    private InternshipProfile internshipProfile;

    // ── Manager đưa ra quyết định ─────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private User manager;

    // ── Đánh giá gốc của Mentor ───────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "final_evaluation_id")
    private FinalEvaluation finalEvaluation;

    // ── Quyết định ────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false)
    private DecisionType decision;

    @Column(name = "manager_comment", columnDefinition = "TEXT")
    private String managerComment;

    // ── Trạng thái xử lý HR ───────────────────────────────────────────────
    @Column(name = "hr_notified", nullable = false)
    private Boolean hrNotified = false;

    @Column(name = "hr_notified_at")
    private LocalDateTime hrNotifiedAt;

    // ── Timestamps ────────────────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ── Enum quyết định ───────────────────────────────────────────────────
    public enum DecisionType {
        /** Đạt — hoàn thành thực tập */
        PASS,
        /** Không đạt — trượt */
        FAIL,
        /** Tuyển dụng chính thức */
        CONVERT_TO_STAFF
    }
}