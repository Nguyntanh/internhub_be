package com.example.internhub_be.controller;

import com.example.internhub_be.payload.request.FinalEvaluationRequest;
import com.example.internhub_be.payload.response.FinalEvaluationResponse;
import com.example.internhub_be.service.FinalEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mentor/evaluations")
@RequiredArgsConstructor
public class FinalEvaluationController {

    private final FinalEvaluationService evaluationService;

    /**
     * GET /api/mentor/evaluations/intern/{internId}
     *
     * Mentor xem bảng điểm tổng hợp từ các Micro-tasks của intern,
     * kèm theo trạng thái đánh giá nếu đã có.
     */
    @GetMapping("/intern/{internId}")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN', 'MANAGER')")
    public ResponseEntity<FinalEvaluationResponse> getEvaluationSummary(
            @PathVariable Long internId) {
        return ResponseEntity.ok(evaluationService.getEvaluationByIntern(internId));
    }

    /**
     * POST /api/mentor/evaluations
     *
     * Tạo mới hoặc cập nhật draft đánh giá.
     * Có thể gọi nhiều lần để lưu nháp trước khi gửi.
     *
     * Body: { "internId": 5, "overallComment": "..." }
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<FinalEvaluationResponse> saveOrUpdateDraft(
            @Valid @RequestBody FinalEvaluationRequest request) {
        String mentorEmail = getCurrentUserEmail();
        FinalEvaluationResponse response = evaluationService.saveOrUpdateDraft(request, mentorEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/mentor/evaluations/{id}/submit
     *
     * Gửi phê duyệt — hành động không thể hoàn tác.
     * Sau khi submit:
     *   - status → SUBMITTED
     *   - is_locked → true  (ngăn submit/review thêm task mới)
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('MENTOR', 'ADMIN')")
    public ResponseEntity<FinalEvaluationResponse> submitEvaluation(
            @PathVariable Long id) {
        String mentorEmail = getCurrentUserEmail();
        FinalEvaluationResponse response = evaluationService.submitEvaluation(id, mentorEmail);
        return ResponseEntity.ok(response);
    }

    private String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
