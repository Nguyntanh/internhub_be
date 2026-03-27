package com.example.internhub_be.controller;

import com.example.internhub_be.payload.request.ManagerDecisionRequest;
import com.example.internhub_be.payload.response.ManagerReviewResponse;
import com.example.internhub_be.service.ManagerReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API dành cho Manager (CEO) xem xét và ra quyết định cuối về kết quả thực tập.
 *
 * Base URL: /api/manager/reviews
 *
 * Endpoints:
 *   GET  /pending           - Danh sách intern chờ duyệt (FinalEvaluation đã SUBMITTED)
 *   GET  /all               - Tất cả hồ sơ (kể cả đã có quyết định)
 *   GET  /{profileId}       - Báo cáo tổng hợp chi tiết của 1 intern
 *   POST /decision          - Manager ra quyết định (PASS / FAIL / CONVERT_TO_STAFF)
 */
@RestController
@RequestMapping("/api/manager/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class ManagerReviewController {

    private final ManagerReviewService reviewService;

    /**
     * GET /api/manager/reviews/pending
     *
     * Trả về danh sách intern có FinalEvaluation đã SUBMITTED
     * và CHƯA có quyết định của Manager.
     * Dùng để hiển thị badge số lượng + danh sách cần xem xét.
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ManagerReviewResponse>> getPendingReviews() {
        return ResponseEntity.ok(
                reviewService.getPendingReviews(getCurrentEmail(), true));
    }

    /**
     * GET /api/manager/reviews/all
     *
     * Tất cả hồ sơ đã SUBMITTED (kể cả đã có quyết định rồi).
     * Dùng cho trang lịch sử / tra cứu.
     */
    @GetMapping("/all")
    public ResponseEntity<List<ManagerReviewResponse>> getAllReviews() {
        return ResponseEntity.ok(
                reviewService.getPendingReviews(getCurrentEmail(), false));
    }

    /**
     * GET /api/manager/reviews/{internshipProfileId}
     *
     * Lấy báo cáo tổng hợp đầy đủ của 1 intern:
     *   - Thông tin cá nhân + hồ sơ thực tập
     *   - Nhận xét của Mentor
     *   - Tổng điểm kỹ năng (radar)
     *   - Lịch sử task đã thực thi
     *   - Quyết định hiện tại (nếu đã có)
     */
    @GetMapping("/{internshipProfileId}")
    public ResponseEntity<ManagerReviewResponse> getReviewDetail(
            @PathVariable Long internshipProfileId) {
        return ResponseEntity.ok(
                reviewService.getReviewDetail(internshipProfileId, getCurrentEmail()));
    }

    /**
     * POST /api/manager/reviews/decision
     *
     * Manager gửi quyết định cuối.
     * Body: { internshipProfileId, decision, managerComment }
     *
     * Sau khi lưu:
     *   - InternshipProfile.status được cập nhật (Completed / Terminated)
     *   - Tất cả HR được nhận thông báo trong hệ thống
     */
    @PostMapping("/decision")
    public ResponseEntity<ManagerReviewResponse> submitDecision(
            @Valid @RequestBody ManagerDecisionRequest request) {
        return ResponseEntity.ok(
                reviewService.submitDecision(request, getCurrentEmail()));
    }

    // ─── Helper ───────────────────────────────────────────────────────────

    private String getCurrentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}