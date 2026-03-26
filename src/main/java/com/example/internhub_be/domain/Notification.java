package com.example.internhub_be.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Thông báo nội bộ — dùng cho cả Manager (khi Mentor submit) và HR (khi Manager quyết định).
 *
 * type phân loại loại thông báo để FE hiển thị icon và link đúng.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người nhận thông báo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // Người gửi / trigger (có thể null nếu do hệ thống tạo)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationType type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    // ID của thực thể liên quan (FinalEvaluation, InternshipDecision, v.v.)
    @Column(name = "reference_id")
    private Long referenceId;

    // Tên thực thể liên quan (VD: "FinalEvaluation", "InternshipDecision")
    @Column(name = "reference_type", length = 50)
    private String referenceType;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ── Enum loại thông báo ───────────────────────────────────────────────
    public enum NotificationType {
        /** Mentor đã gửi đánh giá → Manager cần review */
        EVALUATION_SUBMITTED,
        /** Manager đã quyết định PASS → HR làm thủ tục */
        DECISION_PASS,
        /** Manager đã quyết định FAIL → HR thông báo intern */
        DECISION_FAIL,
        /** Manager quyết định tuyển dụng chính thức → HR làm hợp đồng */
        DECISION_CONVERT_TO_STAFF,
        /** Thông báo chung */
        GENERAL
    }
}